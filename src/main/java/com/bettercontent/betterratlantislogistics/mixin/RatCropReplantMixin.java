package com.bettercontent.betterratlantislogistics.mixin;

import com.bettercontent.betterratlantislogistics.ReplantingInputs;
import com.github.alexthe666.rats.server.entity.ai.goal.harvest.RatHarvestCropsGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RatHarvestCropsGoal.class, remap = false)
abstract class RatCropReplantMixin {
    @Redirect(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"),
        remap = true
    )
    private boolean ratlantisLogistics$consumeSeedBeforeReplant(Level level, BlockPos pos, BlockState state) {
        return ReplantingInputs.replantCrop(level, pos, state);
    }
}
