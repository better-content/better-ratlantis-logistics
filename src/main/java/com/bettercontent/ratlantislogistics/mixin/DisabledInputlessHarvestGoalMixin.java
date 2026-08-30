package com.bettercontent.ratlantislogistics.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {
    "com.github.alexthe666.rats.server.entity.ai.goal.harvest.RatFishermanGoal",
    "com.github.alexthe666.rats.server.entity.ai.goal.harvest.RatMilkCowGoal"
}, remap = false)
public abstract class DisabledInputlessHarvestGoalMixin {
    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true, remap = false)
    private void ratlantisLogistics$disableInputlessHarvest(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
