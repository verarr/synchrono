package xyz.verarr.synchrono;

import java.time.*;
import java.time.temporal.ChronoUnit;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import xyz.verarr.synchrono.config.SynchronoConfig;
import xyz.verarr.synchrono.external_apis.SunriseSunsetAPI;
import xyz.verarr.synchrono.external_apis.SunriseSunsetAPI.SunriseSunsetData;

public class IRLTimeManager extends PersistentState {
    private static final String FIRST_START_DATE_NBT_TAG = "first_start_date";
    private static final int    TICKS_PER_DAY            = 24000;
    private static final int    TICKS_PER_HALF_DAY       = TICKS_PER_DAY / 2;
    private static final int    SERVER_TICKS_PER_SECOND  = 20;

    public LocalDate firstStartDate;

    public IRLTimeManager() { this.firstStartDate = LocalDate.now(ZoneId.of("UTC")); }

    public IRLTimeManager(long firstStartDate) {
        this.firstStartDate = LocalDate.ofEpochDay(firstStartDate);
    }

    private static PersistentStateType<IRLTimeManager> type(ServerWorld world) {
        Codec<IRLTimeManager> codec = RecordCodecBuilder.create(
            instance
            -> instance
                   .group(Codec.LONG.fieldOf(FIRST_START_DATE_NBT_TAG)
                              .forGetter(manager -> manager.firstStartDate.toEpochDay()))
                   .apply(instance, IRLTimeManager::new));

        return new PersistentStateType<>(Synchrono.MOD_ID, IRLTimeManager::new, codec, null);
    }

    public static IRLTimeManager getInstance(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(IRLTimeManager.type(world));
    }

    private SunriseSunsetData querySunriseSunsetAPI(LocalDate localDate) {
        return SunriseSunsetAPI.query(localDate, SynchronoConfig.latitude,
                                      SynchronoConfig.longitude);
    }

    public long tickAt(Instant instant) {
        long ticks;

        long days =
            ChronoUnit.DAYS.between(firstStartDate, instant.atZone(ZoneId.of("UTC")).toLocalDate());
        ticks = days * TICKS_PER_DAY;
        LocalDate yesterday, today, tomorrow;
        yesterday = instant.minus(Duration.ofDays(1)).atZone(ZoneId.of("UTC")).toLocalDate();
        today     = instant.atZone(ZoneId.of("UTC")).toLocalDate();
        tomorrow  = instant.plus(Duration.ofDays(1)).atZone(ZoneId.of("UTC")).toLocalDate();

        SunriseSunsetData yesterday_data, today_data, tomorrow_data;
        yesterday_data = querySunriseSunsetAPI(yesterday);
        today_data     = querySunriseSunsetAPI(today);
        tomorrow_data  = querySunriseSunsetAPI(tomorrow);

        if (instant.isBefore(today_data.sunrise)) {
            // before sunrise - use yesterday_data (and today_data)
            Duration night_length = Duration.between(yesterday_data.sunset, today_data.sunrise);
            Duration since_sunset = Duration.between(yesterday_data.sunset, instant);
            double   tick_scalar  = (double) since_sunset.toMillis() / night_length.toMillis();
            ticks -= (TICKS_PER_HALF_DAY) - (long) (TICKS_PER_HALF_DAY * tick_scalar);
        } else if (instant.isAfter(today_data.sunset)) {
            // after sunset - use tomorrow_data (and today_data)
            Duration night_length  = Duration.between(today_data.sunset, tomorrow_data.sunrise);
            Duration since_sunset  = Duration.between(today_data.sunset, instant);
            double   tick_scalar   = (double) since_sunset.toMillis() / night_length.toMillis();
            ticks                 += TICKS_PER_HALF_DAY + (long) (TICKS_PER_HALF_DAY * tick_scalar);
        } else {
            // daytime - only use today_data
            Duration day_length     = Duration.between(today_data.sunrise, today_data.sunset);
            Duration since_sunrise  = Duration.between(today_data.sunrise, instant);
            double   tick_scalar    = (double) since_sunrise.toMillis() / day_length.toMillis();
            ticks                  += (long) (TICKS_PER_HALF_DAY * tick_scalar);
        }

        if (SynchronoConfig.invert) ticks += TICKS_PER_HALF_DAY;
        ticks  = Math.round(ticks * SynchronoConfig.scalar);
        ticks += SynchronoConfig.offsetTicks;

        if (ticks < 0) ticks = (ticks % TICKS_PER_DAY + TICKS_PER_DAY) % TICKS_PER_DAY;

        return ticks;
    }

    public int daytimeTicksAt(Instant instant) {
        int daytimeTicks;

        if (!SynchronoConfig.invert) daytimeTicks = hardDaytimeTicksAt(instant);
        else daytimeTicks = hardNighttimeTicksAt(instant) + TICKS_PER_HALF_DAY;

        daytimeTicks = (int) Math.round(daytimeTicks / SynchronoConfig.scalar);

        return daytimeTicks;
    }

    public int nighttimeTicksAt(Instant instant) {
        int nighttimeTicks;

        if (!SynchronoConfig.invert) nighttimeTicks = hardNighttimeTicksAt(instant);
        else nighttimeTicks = hardDaytimeTicksAt(instant) + TICKS_PER_HALF_DAY;

        nighttimeTicks = (int) Math.round(nighttimeTicks / SynchronoConfig.scalar);

        return nighttimeTicks;
    }

    private int hardDaytimeTicksAt(Instant instant) {
        LocalDate yesterday, today, tomorrow;
        yesterday = instant.minus(Duration.ofDays(1)).atZone(ZoneId.of("UTC")).toLocalDate();
        today     = instant.atZone(ZoneId.of("UTC")).toLocalDate();
        tomorrow  = instant.plus(Duration.ofDays(1)).atZone(ZoneId.of("UTC")).toLocalDate();

        SunriseSunsetData yesterdayData, todayData, tomorrowData;
        yesterdayData = querySunriseSunsetAPI(yesterday);
        todayData     = querySunriseSunsetAPI(today);
        tomorrowData  = querySunriseSunsetAPI(tomorrow);

        if (instant.isBefore(todayData.sunrise)) {
            // update next daytime aka today
            return (int) (Duration.between(todayData.sunrise, todayData.sunset).toSeconds()
                          * SERVER_TICKS_PER_SECOND);
        } else if (instant.isAfter(todayData.sunset)) {
            // update next daytime aka tomorrow
            return (int) (Duration.between(tomorrowData.sunrise, tomorrowData.sunset).toSeconds()
                          * SERVER_TICKS_PER_SECOND);
        } else {
            // update current daytime
            return (int) (Duration.between(todayData.sunrise, todayData.sunset).toSeconds()
                          * SERVER_TICKS_PER_SECOND);
        }
    }

    private int hardNighttimeTicksAt(Instant instant) {
        LocalDate yesterday, today, tomorrow;
        yesterday = instant.minus(Duration.ofDays(1)).atZone(ZoneId.of("UTC")).toLocalDate();
        today     = instant.atZone(ZoneId.of("UTC")).toLocalDate();
        tomorrow  = instant.plus(Duration.ofDays(1)).atZone(ZoneId.of("UTC")).toLocalDate();

        SunriseSunsetData yesterdayData, todayData, tomorrowData;
        yesterdayData = querySunriseSunsetAPI(yesterday);
        todayData     = querySunriseSunsetAPI(today);
        tomorrowData  = querySunriseSunsetAPI(tomorrow);

        if (instant.isBefore(todayData.sunrise)) {
            // update current nighttime aka yesterday and today
            return (int) (Duration.between(yesterdayData.sunset, todayData.sunrise).toSeconds()
                          * SERVER_TICKS_PER_SECOND);
        } else if (instant.isAfter(todayData.sunset)) {
            // update current nighttime aka today and tomorrow
            return (int) (Duration.between(todayData.sunset, tomorrowData.sunrise).toSeconds()
                          * SERVER_TICKS_PER_SECOND);
        } else {
            // update next nighttime aka today and tomorrow
            return (int) (Duration.between(todayData.sunset, tomorrowData.sunrise).toSeconds()
                          * SERVER_TICKS_PER_SECOND);
        }
    }
}
