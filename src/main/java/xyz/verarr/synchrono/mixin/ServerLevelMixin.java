package xyz.verarr.synchrono.mixin;

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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.eclipseisoffline.customtimecycle.TimeManager;
import xyz.verarr.synchrono.IRLTimeManager;
import xyz.verarr.synchrono.Synchrono;
import xyz.verarr.synchrono.config.SynchronoConfig;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public class ServerLevelMixin {
    @Shadow @Final private ServerWorldProperties worldProperties;
    @Unique private IRLTimeManager irlTimeManager;

    @Unique private Instant lastUpdateTime;
    @Unique private long lastUpdateTimeTicks;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void initializeIRLTimeManager(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<SpecialSpawner> spawners, boolean shouldTickTime, RandomSequencesState randomSequencesState, CallbackInfo ci) {
        irlTimeManager = IRLTimeManager.getInstance((ServerWorld) (Object) this);
        irlTimeManager.markDirty();
    }

    @Unique
    public void updateTime() {
        if (!SynchronoConfig.gametimeEnabled) return;

        lastUpdateTime = Instant.now();
        lastUpdateTimeTicks = worldProperties.getTime();

        TimeManager timeManager = TimeManager.getInstance((ServerWorld) (Object) this);
        LocalDateTime now = LocalDateTime.now(SynchronoConfig.timezone());
        int daytime = irlTimeManager.daytimeTicksAt(now);
        int nighttime = irlTimeManager.nighttimeTicksAt(now);
        if (SynchronoConfig.setRate) {
            Synchrono.LOGGER.info("Setting time rate: {} {}", daytime, nighttime);
            timeManager.setTimeRate(daytime, nighttime);
        }

        long ticks = irlTimeManager.tickAt(LocalDateTime.now(SynchronoConfig.timezone()));
        if (SynchronoConfig.setTime) {
            Synchrono.LOGGER.info("Time is: {}", ticks);
            this.worldProperties.setTimeOfDay(ticks);
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void initialUpdateTime(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<SpecialSpawner> spawners, boolean shouldTickTime, RandomSequencesState randomSequencesState, CallbackInfo ci) {
        updateTime();
    }

    @Inject(method = "tickTime", at = @At("TAIL"))
    public void periodicallyUpdateTime(CallbackInfo ci) {
        String reason;

        long minutesSinceLastUpdate = ChronoUnit.MINUTES.between(lastUpdateTime, Instant.now());
        long serverTicksSinceLastUpdate = worldProperties.getTime() - lastUpdateTimeTicks;
        long wallClockTicksSinceLastUpdate = (ChronoUnit.MILLIS.between(lastUpdateTime, Instant.now()) * 20) / 1000;

        if (minutesSinceLastUpdate >= 30)
            reason = "30 wall clock minutes have passed since last update";
        else if (wallClockTicksSinceLastUpdate >= serverTicksSinceLastUpdate + 200)
            reason = "Time out of sync (" + wallClockTicksSinceLastUpdate + " >= " + serverTicksSinceLastUpdate + " + 200)";
        else if (SynchronoConfig.bruteForce)
            reason = "Brute-force mode enabled";
        else return;

        updateTime();
        Synchrono.LOGGER.info("Time update triggered because: {}", reason);
    }
}
