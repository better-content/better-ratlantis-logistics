package com.bettercontent.ratlantislogistics.mixin;

import com.github.alexthe666.rats.server.block.entity.RatCageWheelBlockEntity;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RatCageWheelBlockEntity.class, remap = false)
abstract class RatWheelEnergyMixin {
    @Inject(method = "getCapability", at = @At("HEAD"), cancellable = true, remap = false)
    private <T> void ratlantisLogistics$disableWheelEnergy(Capability<T> capability, Direction side, CallbackInfoReturnable<LazyOptional<T>> cir) {
        if (capability == ForgeCapabilities.ENERGY) cir.setReturnValue(LazyOptional.empty());
    }
}
