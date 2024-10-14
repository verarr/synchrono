package xyz.verarr.synchrono.mixin;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
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
import xyz.verarr.synchrono.config.NewSynchronoConfig;
import xyz.verarr.synchrono.config.SynchronoConfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class ServerLevelMixin {
    @Shadow public abstract void setTimeOfDay(long timeOfDay);

    @Shadow public abstract PersistentStateManager getPersistentStateManager();

    @Shadow @Final private ServerWorldProperties worldProperties;
    @Unique private IRLTimeManager irlTimeManager;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void initializeIRLTimeManager(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List spawners, boolean shouldTickTime, RandomSequencesState randomSequencesState, CallbackInfo ci) {
        irlTimeManager = getPersistentStateManager().getOrCreate(IRLTimeManager.type, Synchrono.MOD_ID);
        irlTimeManager.markDirty();
    }

    @Unique
    public void updateTime() {
        if (!NewSynchronoConfig.gametime_enabled) return;

        TimeManager timeManager = TimeManager.getInstance((ServerWorld) (Object) this);
        LocalDateTime now = LocalDateTime.now(irlTimeManager.timezone);
        int daytime = irlTimeManager.daytimeTicksAt(now);
        int nighttime = irlTimeManager.nighttimeTicksAt(now);
        if (NewSynchronoConfig.set_rate) {
            Synchrono.LOGGER.info("Setting time rate: {} {}", daytime, nighttime);
            timeManager.setTimeRate(daytime, nighttime);
        }

        long ticks = irlTimeManager.tickAt(LocalDateTime.now(irlTimeManager.timezone));
        if (NewSynchronoConfig.set_time) {
            Synchrono.LOGGER.info("Time is: {}", ticks);
            this.worldProperties.setTimeOfDay(ticks);
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void initialUpdateTime(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List spawners, boolean shouldTickTime, RandomSequencesState randomSequencesState, CallbackInfo ci) {
        updateTime();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void periodicallyUpdateTime(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (
                (worldProperties.getTime() % 12000 == 0) ||
                        NewSynchronoConfig.brute_force
        ) {
            updateTime();
        }
    }
}
