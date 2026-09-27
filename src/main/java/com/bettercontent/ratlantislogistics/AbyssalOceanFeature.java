package com.bettercontent.ratlantislogistics;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.ForgeRegistries;

/** Deepens the existing Ratlantis sea before ores and vegetation are placed. */
public final class AbyssalOceanFeature extends Feature<NoneFeatureConfiguration> {
    private static final int SEA_LEVEL = 63;

    public AbyssalOceanFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        int minX = context.origin().getX() & ~15;
        int minZ = context.origin().getZ() & ~15;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean changed = false;

        for (int x = minX; x < minX + 16; x++) {
            for (int z = minZ; z < minZ + 16; z++) {
                int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                // Surface land is the first-visit source of pirat wood and cheese.
                if (surface > SEA_LEVEL + 1 || !level.getFluidState(cursor.set(x, surface - 1, z)).is(FluidTags.WATER)) {
                    continue;
                }

                int originalFloor = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
                int floor = Math.max(6, Math.min(22, originalFloor - 42));
                if (floor >= originalFloor - 2) continue;

                // Bedrock and a stone foundation remain for Ratlantis ores.
                for (int y = floor + 1; y < SEA_LEVEL; y++) {
                    cursor.set(x, y, z);
                    if (!level.getBlockState(cursor).is(Blocks.WATER)
                            && !level.getBlockState(cursor).is(Blocks.BEDROCK)) {
                        level.setBlock(cursor, Blocks.WATER.defaultBlockState(), 2);
                        changed = true;
                    }
                }
                cursor.set(x, floor, z);
                if (level.getBlockState(cursor).is(Blocks.STONE) && ((x * 31 + z * 17) & 7) == 0) {
                    level.setBlock(cursor, Blocks.GRAVEL.defaultBlockState(), 2);
                }
            }
        }
        if (changed && context.random().nextInt(32) == 0) {
            placePrismarineReef(level, minX + 8, minZ + 8, cursor);
        }
        return changed;
    }

    private static void placePrismarineReef(WorldGenLevel level, int x, int z, BlockPos.MutableBlockPos cursor) {
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
        if (surface - floor < 35 || floor < 6 || floor > 22) return;
        ResourceLocation coralId = new ResourceLocation("upgrade_aquatic", "prismarine_coral_block");
        if (!ForgeRegistries.BLOCKS.containsKey(coralId)) return;
        Block coral = ForgeRegistries.BLOCKS.getValue(coralId);
        level.setBlock(cursor.set(x, floor, z), Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) > 3 || dx == 0 && dz == 0) continue;
                cursor.set(x + dx, floor, z + dz);
                if (level.getBlockState(cursor).is(Blocks.BEDROCK)) continue;
                level.setBlock(cursor, Blocks.STONE.defaultBlockState(), 2);
                cursor.set(x + dx, floor + 1, z + dz);
                if (level.getFluidState(cursor).is(FluidTags.WATER)) {
                    level.setBlock(cursor, coral.defaultBlockState(), 2);
                }
            }
        }
    }
}
