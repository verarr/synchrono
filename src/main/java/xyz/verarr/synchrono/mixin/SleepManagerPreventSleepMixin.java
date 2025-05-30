package xyz.verarr.synchrono.mixin;

import net.minecraft.server.world.SleepManager;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.verarr.synchrono.config.SynchronoConfig;

@Mixin(SleepManager.class)
public class SleepManagerPreventSleepMixin {
    @ModifyReturnValue(method = "canSkipNight(I)Z", at = @At("RETURN"))
    public boolean preventSleep(boolean original) {
        return SynchronoConfig.preventSleep && SynchronoConfig.gametimeEnabled ? false : original;
    }
}
