package xyz.verarr.synchrono.mixin;

import net.minecraft.server.world.ServerWorld;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.verarr.synchrono.config.SynchronoConfig;

@Mixin(ServerWorld.class)
public class CosmeticPreventSleepMixin {
    @ModifyReturnValue(method = "isSleepingEnabled()Z", at = @At("RETURN"))
    public boolean isSleepingEnabled(boolean original) {
        return SynchronoConfig.preventSleep && SynchronoConfig.gametimeEnabled ? false : original;
    }
}
