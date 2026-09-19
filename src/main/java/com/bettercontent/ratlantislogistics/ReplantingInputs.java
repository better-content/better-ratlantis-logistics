package com.bettercontent.ratlantislogistics;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public final class ReplantingInputs {
    private static final TagKey<Item> CULTIVAR_SEEDS = TagKey.create(
        Registries.ITEM,
        new ResourceLocation("bumblezone_cultivars", "seeds")
    );

    private ReplantingInputs() {}

    /**
     * Replants with one propagule for this exact plant.  The Rats goal has already
     * harvested the mature block when its replacement call is redirected, so use
     * the replacement state instead of looking at the now-empty world position.
     *
     * The item is deliberately debited only after {@link Level#setBlockAndUpdate}
     * succeeds.  That keeps a rejected placement from eating a nearby propagule.
     */
    public static boolean replantCrop(Level level, BlockPos pos, BlockState state) {
        Item requiredPropagule = state.getBlock().asItem();
        if (requiredPropagule == null) return false;

        ItemEntity candidate = nearest(level, pos, new AABB(pos).inflate(1.5D), requiredPropagule);
        if (candidate == null || !level.setBlockAndUpdate(pos, state)) return false;
        consume(candidate);
        return true;
    }

    public static boolean consumeSapling(Level level, BlockPos pos, Item sapling) {
        return consumeNearest(level, pos, new AABB(pos).inflate(12.0D, 32.0D, 12.0D), sapling);
    }

    private static boolean consumeNearest(Level level, BlockPos pos, AABB bounds, @Nullable Item requiredItem) {
        ItemEntity candidate = nearest(level, pos, bounds, requiredItem);
        if (candidate == null) return false;
        consume(candidate);
        return true;
    }

    @Nullable
    private static ItemEntity nearest(Level level, BlockPos pos, AABB bounds, @Nullable Item requiredItem) {
        return level.getEntitiesOfClass(ItemEntity.class, bounds, entity -> {
                var stack = entity.getItem();
                return !stack.isEmpty() && stack.is(CULTIVAR_SEEDS) && (requiredItem == null || stack.is(requiredItem));
            }).stream()
            .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)))
            .orElse(null);
    }

    private static void consume(ItemEntity candidate) {
        candidate.getItem().shrink(1);
        if (candidate.getItem().isEmpty()) candidate.discard();
    }
}
