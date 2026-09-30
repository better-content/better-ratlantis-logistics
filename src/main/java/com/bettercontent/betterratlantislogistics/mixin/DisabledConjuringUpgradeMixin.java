package com.bettercontent.betterratlantislogistics.mixin;

import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Creative-obtained legacy upgrades remain inert instead of producing free resources. */
@Mixin(targets = {
    "com.github.alexthe666.rats.server.items.upgrades.AristocratRatUpgradeItem",
    "com.github.alexthe666.rats.server.items.upgrades.ChristmasRatUpgradeItem",
    "com.github.alexthe666.rats.server.items.upgrades.SupportRatUpgradeItem"
}, remap = false)
public abstract class DisabledConjuringUpgradeMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
    private void ratlantisLogistics$disableConjuredOutput(TamedRat rat, CallbackInfo ci) {
        ci.cancel();
    }
}
