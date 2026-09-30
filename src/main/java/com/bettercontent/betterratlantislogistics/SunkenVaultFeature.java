package com.bettercontent.betterratlantislogistics;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.ForgeRegistries;

/** A compact, flooded Ratlantean ruin that fits inside one chunk. */
public final class SunkenVaultFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation LOOT = new ResourceLocation(RatlantisLogistics.MOD_ID, "chests/sunken_vault");

    public SunkenVaultFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        int x = (context.origin().getX() & ~15) + 8;
        int z = (context.origin().getZ() & ~15) + 8;
        int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        if (floor < 6 || floor > 22 || surface - floor < 35) return false;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (!level.getFluidState(cursor.set(x + dx, floor + 1, z + dz)).is(FluidTags.WATER)) {
                    return false;
                }
            }
        }

        Block marble = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("rats", "marbled_cheese_brick"));
        Block mossy = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("rats", "marbled_cheese_brick_mossy"));
        if (marble == null || mossy == null) return false;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) == 3 && Math.abs(dz) == 3) continue;
                level.setBlock(cursor.set(x + dx, floor, z + dz),
                        (Math.abs(dx * 31 + dz * 17) % 5 == 0 ? mossy : marble).defaultBlockState(), 2);
            }
        }
        for (int dx : new int[]{-2, 2}) {
            for (int dz : new int[]{-2, 2}) {
                for (int dy = 1; dy <= 4; dy++) {
                    level.setBlock(cursor.set(x + dx, floor + dy, z + dz),
                            (dy == 4 ? mossy : marble).defaultBlockState(), 2);
                }
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            level.setBlock(cursor.set(x + dx, floor + 4, z - 2), marble.defaultBlockState(), 2);
            level.setBlock(cursor.set(x + dx, floor + 4, z + 2), mossy.defaultBlockState(), 2);
        }
        BlockPos barrelPos = new BlockPos(x, floor + 1, z);
        level.setBlock(barrelPos, Blocks.BARREL.defaultBlockState(), 2);
        if (level.getBlockEntity(barrelPos) instanceof BarrelBlockEntity barrel) {
            barrel.setLootTable(LOOT, context.random().nextLong());
            barrel.setChanged();
        }
        return true;
    }
}
