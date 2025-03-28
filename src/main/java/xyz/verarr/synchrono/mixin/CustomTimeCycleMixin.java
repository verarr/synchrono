package xyz.verarr.synchrono.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.eclipseisoffline.customtimecycle.CustomTimeCycle;
import xyz.verarr.synchrono.Synchrono;
import xyz.verarr.synchrono.config.SynchronoConfig;

@Mixin(CustomTimeCycle.class)
public class CustomTimeCycleMixin {
    @WrapWithCondition(
        method = "onInitialize()V",
        at     = @At(value  = "INVOKE",
                     target = "Lnet/fabricmc/fabric/api/event/Event;register(Ljava/lang/Object;)V"),
        remap  = false)
    private boolean
    doNotRegister(Event<CommandRegistrationCallback> instance, Object object) {
        if (!SynchronoConfig.removeCommands) {
            Synchrono.LOGGER.info("Not removing commands");
            return true;
        }

        if (!(object instanceof CommandRegistrationCallback)) {
            Synchrono.LOGGER.warn("Not a CommandRegistrationCallback ({})", object);
            return true;
        }

        Synchrono.LOGGER.info("Removing command {}", object);
        return false;
    }
}
