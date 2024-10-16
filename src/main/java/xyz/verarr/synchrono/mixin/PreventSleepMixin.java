package xyz.verarr.synchrono.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.SleepManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.verarr.synchrono.config.SynchronoConfig;

@Mixin(ServerWorld.class)
public class PreventSleepMixin {
    @ModifyExpressionValue(method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/SleepManager;canSkipNight(I)Z"))
    public boolean preventSleeping(boolean original) {
        return SynchronoConfig.prevent_sleep ? false : original;
    }

    /**
     * @author verarr
     * @reason cosmetic messages relating to sleep
     */
    @ModifyReturnValue(method = "isSleepingEnabled()Z", at = @At("RETURN"))
    public boolean isSleepingEnabled(boolean original) {
        return SynchronoConfig.prevent_sleep ? false : original;
    }
}
