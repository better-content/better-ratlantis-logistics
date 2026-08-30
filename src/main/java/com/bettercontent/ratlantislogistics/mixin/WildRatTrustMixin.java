package com.bettercontent.ratlantislogistics.mixin;

import com.bettercontent.ratlantislogistics.RatlantisLogistics;
import com.github.alexthe666.rats.server.entity.ai.goal.WildRatTargetFoodGoal;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Dropped food remains valid rat food, but only Ratlantean bait builds taming trust. */
@Mixin(value = WildRatTargetFoodGoal.class, remap = false)
public abstract class WildRatTrustMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z"), remap = true)
    private boolean ratlantisLogistics$baitOnlyTrust(ItemStack stack, TagKey<Item> ignoredCheeseTag) {
        return stack.is(RatlantisLogistics.RATLANTEAN_BAIT.get());
    }
}
