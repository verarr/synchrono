package xyz.verarr.synchrono.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.eclipseisoffline.customtimecycle.CustomTimeCycle;

@Mixin(CustomTimeCycle.class)
public class CustomTimeCycleMixin {
    @WrapWithCondition(
            method = "onInitialize()V",
            at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/event/Event;register(Ljava/lang/Object;)V"),
            remap = false
    )
    private boolean doNotRegister(Event<CommandRegistrationCallback> instance, Object object) {
        return !(object instanceof CommandRegistrationCallback);
    }
}
