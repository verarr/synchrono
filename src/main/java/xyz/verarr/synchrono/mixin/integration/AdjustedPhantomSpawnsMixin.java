package xyz.verarr.synchrono.mixin.integration;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import xyz.verarr.adjusted_phantom_spawns.GameRuleHelper;
import xyz.verarr.synchrono.config.SynchronoConfig;

@Mixin(GameRuleHelper.class)
public class AdjustedPhantomSpawnsMixin {
    @WrapMethod(method = "getPhantomSpawningThreshold", remap = false)
    private int wrapGetPhantomSpawningThreshold(Operation<Integer> original) {
        if (SynchronoConfig.adjustedPhantomSpawnsIntegration) return 1728000;
        else return original.call();
    }
}
