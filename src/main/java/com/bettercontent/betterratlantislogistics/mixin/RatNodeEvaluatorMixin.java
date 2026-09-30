package com.bettercontent.betterratlantislogistics.mixin;

import com.github.alexthe666.rats.server.block.RatCageBlock;
import com.github.alexthe666.rats.server.block.RatTubeBlock;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.entity.ai.navigation.evaluator.RatNodeEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Makes the entire connected tube/cage graph traversable after a rat enters it. */
@Mixin(value = RatNodeEvaluator.class, remap = false)
public abstract class RatNodeEvaluatorMixin extends WalkNodeEvaluator {

    @Inject(method = "evaluateBlockPathType", at = @At("RETURN"), cancellable = true, remap = false)
    private void ratlantisLogistics$connectedTubeNodes(BlockGetter level, BlockPos pos, BlockPathTypes original,
                                                       CallbackInfoReturnable<BlockPathTypes> cir) {
        if (mob instanceof TamedRat rat && (rat.isInTube() || rat.isInCage())) {
            var block = level.getBlockState(pos).getBlock();
            if (block instanceof RatTubeBlock || block instanceof RatCageBlock) {
                cir.setReturnValue(BlockPathTypes.WALKABLE);
            }
        }
    }
}
