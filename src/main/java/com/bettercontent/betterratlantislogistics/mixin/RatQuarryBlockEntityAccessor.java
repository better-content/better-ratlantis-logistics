package com.bettercontent.betterratlantislogistics.mixin;

import com.github.alexthe666.rats.server.block.entity.RatQuarryBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RatQuarryBlockEntity.class, remap = false)
public interface RatQuarryBlockEntityAccessor {
    @Accessor(value = "tick", remap = false) int ratlantisLogistics$getTick();
}
