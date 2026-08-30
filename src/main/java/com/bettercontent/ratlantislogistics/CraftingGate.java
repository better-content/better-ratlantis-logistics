package com.bettercontent.ratlantislogistics;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

public final class CraftingGate {
    private static final Set<String> CREATE_INTERMEDIATE = Set.of("packager", "repackager", "stock_link", "stock_ticker", "redstone_requester", "package_frogport", "chain_conveyor");
    private static final Set<String> AE2_ROOTS = Set.of("controller", "energy_acceptor", "vibration_chamber");
    private static final Set<String> RATS_BASIC = Set.of("rat_cage", "rat_cage_breeding_lantern", "rat_tube", "rat_upgrade_basic", "rat_upgrade_mount_basic", "rat_upgrade_farmer", "rat_upgrade_crafting");
    private CraftingGate() {}

    @SubscribeEvent
    public static void onCraft(PlayerEvent.ItemCraftedEvent event) {
        ResourceLocation result = ForgeRegistries.ITEMS.getKey(event.getCrafting().getItem());
        Tier tier = tier(result);
        if (tier == null || event.getEntity().getAbilities().instabuild) return;
        Item component = tier.item();
        var inventory = event.getEntity().getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(component)) { stack.shrink(1); return; }
        }
        event.getCrafting().setCount(0);
        event.getEntity().displayClientMessage(Component.translatable("message.ratlantis_logistics.missing_component", component.getDescription()), true);
    }

    static Tier tier(ResourceLocation id) {
        if (id == null || id.getNamespace().equals(RatlantisLogistics.MOD_ID)) return null;
        String namespace = id.getNamespace(), path = id.getPath();
        if (namespace.equals("ae2") && AE2_ROOTS.contains(path)) return Tier.RARE;
        if (namespace.equals("create") && CREATE_INTERMEDIATE.contains(path)) return Tier.INTERMEDIATE;
        if (namespace.equals("prettypipes")) {
            if (path.startsWith("high_") || path.contains("terminal") || path.equals("pressurizer")) return Tier.RARE;
            if (path.equals("pipe") || path.equals("wrench") || path.equals("pipe_frame")) return Tier.BULK;
            return Tier.INTERMEDIATE;
        }
        if (namespace.equals("rats")) {
            if (RATS_BASIC.contains(path) || path.contains("cage") || path.contains("tube")) return Tier.BULK;
            if (path.startsWith("rat_upgrade") || path.contains("ratlantis_upgrade")) return Tier.INTERMEDIATE;
        }
        if (namespace.equals("sophisticatedstorage") || namespace.equals("sophisticatedbackpacks")) {
            boolean base = path.equals("backpack") || path.equals("chest") || path.equals("barrel") || path.startsWith("limited_barrel");
            if (base) return Tier.BULK;
            boolean rare = path.contains("advanced_") || path.contains("netherite") || path.contains("auto_") || path.contains("infinity") || path.contains("pump") || path.contains("pickup") || path.contains("hopper") || path.contains("magnet") || path.contains("void") || path.contains("restock") || path.contains("deposit") || path.contains("refill") || path.contains("tool_swapper");
            return rare ? Tier.RARE : Tier.INTERMEDIATE;
        }
        return null;
    }

    enum Tier {
        BULK { Item item() { return RatlantisLogistics.COURIER_LATTICE.get(); } },
        INTERMEDIATE { Item item() { return RatlantisLogistics.ORATCHALCUM_MECHANISM.get(); } },
        RARE { Item item() { return RatlantisLogistics.ARCANE_LOGISTICS_CORE.get(); } };
        abstract Item item();
    }
}
