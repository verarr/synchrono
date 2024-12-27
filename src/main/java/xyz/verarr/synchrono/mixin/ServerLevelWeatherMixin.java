package xyz.verarr.synchrono.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.verarr.synchrono.Synchrono;
import xyz.verarr.synchrono.config.SynchronoConfig;
import xyz.verarr.synchrono.external_apis.OpenMeteoAPI;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executor;

@Mixin(ServerWorld.class)
public class ServerLevelWeatherMixin {
    @Shadow @Final private ServerWorldProperties worldProperties;
    @Unique private Instant lastUpdateWeather;

    @Unique
    public void updateWeather() {
        // TODO: add config check

        lastUpdateWeather = Instant.now();

        OpenMeteoAPI.WeatherCode weatherCode = OpenMeteoAPI.queryCurrent(SynchronoConfig.latitude, SynchronoConfig.longitude);

        // TODO: more weather models (with mods)
        if (weatherCode.thunderstormStrength() > 0 || weatherCode.hailStrength() > 0) {
            worldProperties.setRaining(true);  // unsure if this is necessary
            worldProperties.setThundering(true);
        } else if (weatherCode.rainStrength() > 0 || weatherCode.snowStrength() > 0 || weatherCode.sleetStrength() > 0) {
            worldProperties.setRaining(true);
            worldProperties.setThundering(false);
        } else {
            worldProperties.setRaining(false);
            worldProperties.setThundering(false);
        }

        Synchrono.LOGGER.info("Weather code is: {}", weatherCode.getRawValue());
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void initialUpdateWeather(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<SpecialSpawner> spawners, boolean shouldTickTime, RandomSequencesState randomSequencesState, CallbackInfo ci) {
        updateWeather();
    }

    @Inject(method = "tickWeather", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    public void periodicallyUpdateWeather(CallbackInfo ci) {
        String reason;

        long minutesSinceLastUpdate = ChronoUnit.MINUTES.between(lastUpdateWeather, Instant.now());

        if (minutesSinceLastUpdate >= 5)
            reason = "5 wall clock minutes have passed since last update";
        else return;

        updateWeather();
        Synchrono.LOGGER.info("Weather update triggered because: {}", reason);
    }

    @ModifyExpressionValue(method = "tickWeather", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    public boolean doDaylightCycle(boolean original) {
        // TODO: ADD CONFIG CHECK !!
        return false;
    }
}
