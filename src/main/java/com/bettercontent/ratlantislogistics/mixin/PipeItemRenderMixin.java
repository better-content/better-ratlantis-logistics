package com.bettercontent.ratlantislogistics.mixin;

import com.bettercontent.ratlantislogistics.client.RatCourierRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import de.ellpeck.prettypipes.network.PipeItem;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(value = PipeItem.class, remap = false)
abstract class PipeItemRenderMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private void ratlantisLogistics$renderCourier(PipeBlockEntity pipe, PoseStack pose, Random random, float partialTicks, int light, int overlay, MultiBufferSource buffers, CallbackInfo ci) {
        RatCourierRenderer.render((PipeItem) (Object) this, pipe, pose, partialTicks, light, buffers);
        ci.cancel();
    }
}
