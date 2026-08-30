package com.bettercontent.ratlantislogistics.mixin;

import com.github.alexthe666.rats.registry.RatsBlockRegistry;
import com.github.alexthe666.rats.server.block.entity.RatQuarryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A quarry stair is construction, not a free block generator. */
@Mixin(value = RatQuarryBlockEntity.class, remap = false)
public abstract class RatQuarryBlockEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void ratlantisLogistics$consumePlatform(Level level, BlockPos pos, BlockState state,
                                                           RatQuarryBlockEntity quarry, CallbackInfo ci) {
        if (level.isClientSide() || (((RatQuarryBlockEntityAccessor) quarry).ratlantisLogistics$getTick() + 1) % 20 != 0) return;
        BlockPos next = quarry.getNextPosForStairs(level);
        if (!level.isEmptyBlock(next) && !level.getBlockState(next).canBeReplaced()) return;
        for (int slot = 0; slot < quarry.getContainerSize(); slot++) {
            if (quarry.getItem(slot).is(RatsBlockRegistry.RAT_QUARRY_PLATFORM.get().asItem())) {
                quarry.removeItem(slot, 1);
                quarry.setChanged();
                return;
            }
        }
        ci.cancel();
    }
}
