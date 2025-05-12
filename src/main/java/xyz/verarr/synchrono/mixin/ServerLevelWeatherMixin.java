package xyz.verarr.synchrono.mixin;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executor;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.spawner.SpecialSpawner;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.verarr.synchrono.Synchrono;
import xyz.verarr.synchrono.config.SynchronoConfig;
import xyz.verarr.synchrono.external_apis.OpenMeteoAPI;
import xyz.verarr.synchrono.weather_models.VanillaWeatherModel;
import xyz.verarr.synchrono.weather_models.WeatherModel;

@Mixin(ServerWorld.class)
public class ServerLevelWeatherMixin {
    @Unique private Instant lastUpdateWeather = Instant.MIN;

    @Unique
    public void updateWeather() {
        lastUpdateWeather = Instant.now();

        OpenMeteoAPI.WeatherCode weatherCode =
            OpenMeteoAPI.queryCurrent(SynchronoConfig.latitude, SynchronoConfig.longitude);

        // TODO: more weather models (with mods)
        WeatherModel weatherModel = switch (SynchronoConfig.weatherModel) {
            case VANILLA -> VanillaWeatherModel.getInstance();
        };
        weatherModel.apply(weatherCode, (ServerWorld) (Object) this);

        Synchrono.LOGGER.info("Weather code is: {}", weatherCode.getRawValue());
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void
    initialUpdateWeather(MinecraftServer                 server,
                         Executor                        workerExecutor,
                         LevelStorage.Session            session,
                         ServerWorldProperties           properties,
                         RegistryKey<World>              worldKey,
                         DimensionOptions                dimensionOptions,
                         WorldGenerationProgressListener worldGenerationProgressListener,
                         boolean                         debugWorld,
                         long                            seed,
                         List<SpecialSpawner>            spawners,
                         boolean                         shouldTickTime,
                         RandomSequencesState            randomSequencesState,
                         CallbackInfo                    ci) {
        if (SynchronoConfig.weatherEnabled) updateWeather();
    }

    @Inject(
        method = "tickWeather",
        at     = @At(
            value = "INVOKE",
            shift = At.Shift.AFTER,
            target =
                "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    public void
    periodicallyUpdateWeather(CallbackInfo ci) {
        if (!SynchronoConfig.weatherEnabled) {
            lastUpdateWeather = Instant.MIN;
            return;
        };

        String reason;

        long minutesSinceLastUpdate = ChronoUnit.MINUTES.between(lastUpdateWeather, Instant.now());

        if (minutesSinceLastUpdate >= 5)
            reason = "5 wall clock minutes have passed since last update";
        else return;

        updateWeather();
        Synchrono.LOGGER.info("Weather update triggered because: {}", reason);
    }

    @ModifyExpressionValue(
        method = "tickWeather",
        at     = @At(
            value = "INVOKE",
            target =
                "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    public boolean
    doDaylightCycle(boolean original) {
        return SynchronoConfig.weatherEnabled ? false : original;
    }
}
