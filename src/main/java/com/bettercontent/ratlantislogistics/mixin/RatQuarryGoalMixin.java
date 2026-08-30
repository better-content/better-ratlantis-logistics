package com.bettercontent.ratlantislogistics.mixin;

import com.github.alexthe666.rats.server.entity.ai.goal.harvest.RatQuarryGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Prevents quarry rats from conjuring water and sponges while escaping fluids. */
@Mixin(value = RatQuarryGoal.class, remap = false)
public abstract class RatQuarryGoalMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean ratlantisLogistics$doNotConjure(Level level, BlockPos pos, BlockState state) {
        return false;
    }
}
