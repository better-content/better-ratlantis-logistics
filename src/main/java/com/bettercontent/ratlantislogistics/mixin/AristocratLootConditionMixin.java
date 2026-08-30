package com.bettercontent.ratlantislogistics.mixin;

import com.github.alexthe666.rats.server.loot.RatKilledAndHasUpgradeCondition;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RatKilledAndHasUpgradeCondition.class, remap = false)
public abstract class AristocratLootConditionMixin {
    @Inject(method = "test", at = @At("HEAD"), cancellable = true, remap = false)
    private void ratlantisLogistics$disableAristocratCoins(LootContext context, CallbackInfoReturnable<Boolean> cir) {
        var condition = (RatKilledAndHasUpgradeCondition) (Object) this;
        var id = ForgeRegistries.ITEMS.getKey(condition.upgrade());
        if (id != null && id.toString().equals("rats:rat_upgrade_aristocrat")) cir.setReturnValue(false);
    }
}
