package com.bettercontent.ratlantislogistics.mixin;

import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.items.upgrades.OreDoublingRatUpgradeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TamedRat.class, remap = false)
public abstract class OreDoublingMixin {
    @Redirect(method = "onItemEaten", at = @At(value = "INVOKE", target = "Lcom/github/alexthe666/rats/server/items/upgrades/OreDoublingRatUpgradeItem;isProcessable(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Z"), remap = false)
    private boolean ratlantisLogistics$disableOreDoubling(Level level, ItemStack stack) {
        return false;
    }
}
