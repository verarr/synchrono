package xyz.verarr.synchrono;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import xyz.verarr.synchrono.config.SynchronoConfig;

import java.time.*;
import java.time.temporal.ChronoUnit;

import xyz.verarr.synchrono.external_apis.SunriseSunsetAPI;
import xyz.verarr.synchrono.external_apis.SunriseSunsetAPI.SunriseSunsetData;

public class IRLTimeManager extends PersistentState {
    private static final String FIRST_START_DATE_NBT_TAG = "first_start_date";
    private static final int TICKS_PER_DAY = 24000;
    private static final int TICKS_PER_HALF_DAY = TICKS_PER_DAY / 2;
    private static final int SERVER_TICKS_PER_SECOND = 20;

    public LocalDate firstStartDate;

    public IRLTimeManager() {
        this.firstStartDate = LocalDate.now(SynchronoConfig.timezone());
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putLong(FIRST_START_DATE_NBT_TAG, firstStartDate.toEpochDay());
        return nbt;
    }

    public static IRLTimeManager createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        IRLTimeManager irlTimeManager = new IRLTimeManager();
        irlTimeManager.firstStartDate = LocalDate.ofEpochDay(tag.getLong(FIRST_START_DATE_NBT_TAG));
        return irlTimeManager;
    }

    public static Type<IRLTimeManager> type = new Type<>(
            IRLTimeManager::new,
            IRLTimeManager::createFromNbt,
            null
    );

    public static IRLTimeManager getInstance(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(IRLTimeManager.type, Synchrono.MOD_ID);
    }

    private SunriseSunsetData querySunriseSunsetAPI(LocalDate localDate) {
        return SunriseSunsetAPI.query(localDate, SynchronoConfig.latitude, SynchronoConfig.longitude, SynchronoConfig.timezone());
    }

    public long tickAt(LocalDateTime dateTime) {
        long ticks;

        long days = ChronoUnit.DAYS.between(firstStartDate, dateTime.toLocalDate());
        ticks = days * TICKS_PER_DAY;
        LocalDate yesterday, today, tomorrow;
        yesterday = dateTime.minusDays(1).toLocalDate();
        today = dateTime.toLocalDate();
        tomorrow = dateTime.plusDays(1).toLocalDate();

        SunriseSunsetData yesterday_data, today_data, tomorrow_data;
        yesterday_data = querySunriseSunsetAPI(yesterday);
        today_data = querySunriseSunsetAPI(today);
        tomorrow_data = querySunriseSunsetAPI(tomorrow);

        if (dateTime.isBefore(dateTime.toLocalDate().atTime(today_data.sunrise))) {
            // before sunrise - use yesterday_data (and today_data)
            Duration night_length = Duration.between(yesterday.atTime(yesterday_data.sunset), today.atTime(today_data.sunrise));
            Duration since_sunset = Duration.between(yesterday.atTime(yesterday_data.sunset), dateTime);
            double tick_scalar = (double) since_sunset.toMillis() / night_length.toMillis();
            ticks -= (TICKS_PER_HALF_DAY) - (long) (TICKS_PER_HALF_DAY * tick_scalar);
        } else if (dateTime.isAfter(today.atTime(today_data.sunset))) {
            // after sunset - use tomorrow_data (and today_data)
            Duration night_length = Duration.between(today.atTime(today_data.sunset), tomorrow.atTime(tomorrow_data.sunrise));
            Duration since_sunset = Duration.between(today.atTime(today_data.sunset), dateTime);
            double tick_scalar = (double) since_sunset.toMillis() / night_length.toMillis();
            ticks += TICKS_PER_HALF_DAY + (long) (TICKS_PER_HALF_DAY * tick_scalar);
        } else {
            // daytime - only use today_data
            Duration day_length = Duration.between(today.atTime(today_data.sunrise), today.atTime(today_data.sunset));
            Duration since_sunrise = Duration.between(today.atTime(today_data.sunrise), dateTime);
            double tick_scalar = (double) since_sunrise.toMillis() / day_length.toMillis();
            ticks += (long) (TICKS_PER_HALF_DAY * tick_scalar);
        }

        if (SynchronoConfig.invert) ticks += TICKS_PER_HALF_DAY;
        ticks = Math.round(ticks * SynchronoConfig.scalar);
        ticks += SynchronoConfig.offsetTicks;

        if (ticks < 0) ticks = (ticks % TICKS_PER_DAY + TICKS_PER_DAY) % TICKS_PER_DAY;

        return ticks;
    }

    public int daytimeTicksAt(LocalDateTime dateTime) {
        int daytimeTicks;

        if (!SynchronoConfig.invert) daytimeTicks = hardDaytimeTicksAt(dateTime);
        else daytimeTicks = hardNighttimeTicksAt(dateTime) + TICKS_PER_HALF_DAY;

        daytimeTicks = (int) Math.round(daytimeTicks / SynchronoConfig.scalar);

        return daytimeTicks;
    }

    public int nighttimeTicksAt(LocalDateTime dateTime) {
        int nighttimeTicks;

        if (!SynchronoConfig.invert) nighttimeTicks = hardNighttimeTicksAt(dateTime);
        else nighttimeTicks = hardDaytimeTicksAt(dateTime) + TICKS_PER_HALF_DAY;

        nighttimeTicks = (int) Math.round(nighttimeTicks / SynchronoConfig.scalar);

        return nighttimeTicks;
    }

    private int hardDaytimeTicksAt(LocalDateTime dateTime) {
        LocalDate yesterday, today, tomorrow;
        yesterday = dateTime.minusDays(1).toLocalDate();
        today = dateTime.toLocalDate();
        tomorrow = dateTime.plusDays(1).toLocalDate();

        SunriseSunsetData yesterdayData, todayData, tomorrowData;
        yesterdayData = querySunriseSunsetAPI(yesterday);
        todayData = querySunriseSunsetAPI(today);
        tomorrowData = querySunriseSunsetAPI(tomorrow);

        if (dateTime.isBefore(today.atTime(todayData.sunrise))) {
            // update next daytime aka today
            return (int) Duration.between(today.atTime(todayData.sunrise), today.atTime(todayData.sunset)).toSeconds() * SERVER_TICKS_PER_SECOND;
        } else if (dateTime.isAfter(today.atTime(todayData.sunset))) {
            // update next daytime aka tomorrow
            return (int) Duration.between(tomorrowData.sunrise, tomorrowData.sunset).toSeconds() * SERVER_TICKS_PER_SECOND;
        } else {
            // update current daytime
            return (int) Duration.between(todayData.sunrise, todayData.sunset).toSeconds() * SERVER_TICKS_PER_SECOND;
        }
    }

    private int hardNighttimeTicksAt(LocalDateTime dateTime) {
        LocalDate yesterday, today, tomorrow;
        yesterday = dateTime.minusDays(1).toLocalDate();
        today = dateTime.toLocalDate();
        tomorrow = dateTime.plusDays(1).toLocalDate();

        SunriseSunsetData yesterdayData, todayData, tomorrowData;
        yesterdayData = querySunriseSunsetAPI(yesterday);
        todayData = querySunriseSunsetAPI(today);
        tomorrowData = querySunriseSunsetAPI(tomorrow);

        if (dateTime.isBefore(today.atTime(todayData.sunrise))) {
            // update current nighttime aka yesterday and today
            return (int) Duration.between(yesterday.atTime(yesterdayData.sunset), today.atTime(todayData.sunrise)).toSeconds() * SERVER_TICKS_PER_SECOND;
        } else if (dateTime.isAfter(today.atTime(todayData.sunset))) {
            // update current nighttime aka today and tomorrow
            return (int) Duration.between(today.atTime(todayData.sunset), tomorrow.atTime(tomorrowData.sunrise)).toSeconds() * SERVER_TICKS_PER_SECOND;
        } else {
            // update next nighttime aka today and tomorrow
            return (int) Duration.between(yesterday.atTime(todayData.sunset), today.atTime(tomorrowData.sunrise)).toSeconds() * SERVER_TICKS_PER_SECOND;
        }
    }
}
