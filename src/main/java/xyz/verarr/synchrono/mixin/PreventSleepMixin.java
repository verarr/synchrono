package xyz.verarr.synchrono.mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.SleepManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerWorld.class)
public class PreventSleepMixin {
    @Redirect(method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/SleepManager;canSkipNight(I)Z"))
    public boolean preventSleeping(SleepManager instance, int percentage) {
        return false; // always prevent sleeping
    }

    /**
     * @author verarr
     * @reason cosmetic messages relating to sleep
     */
    @Overwrite()
    public boolean isSleepingEnabled() {
        return false; // always say can't sleep
    }
}
