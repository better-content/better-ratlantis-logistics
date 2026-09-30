package com.bettercontent.betterratlantislogistics;

import com.github.alexthe666.rats.server.entity.rat.Rat;
import com.github.alexthe666.rats.server.misc.RatUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(RatlantisLogistics.MOD_ID)
public final class RatlantisLogistics {
    public static final String MOD_ID = "better_ratlantis_logistics";
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, MOD_ID);
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> ABYSSAL_OCEAN =
            FEATURES.register("abyssal_ocean", () -> new AbyssalOceanFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SUNKEN_VAULT =
            FEATURES.register("sunken_vault", () -> new SunkenVaultFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Item> COURIER_LATTICE = ITEMS.register("courier_lattice", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ORATCHALCUM_MECHANISM = ITEMS.register("oratchalcum_mechanism", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ARCANE_LOGISTICS_CORE = ITEMS.register("arcane_logistics_core", () -> new Item(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> RATLANTEAN_BAIT = ITEMS.register("ratlantean_bait", () -> new Item(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(1).fast().build())));

    public RatlantisLogistics() {
        var modBus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modBus);
        FEATURES.register(modBus);
        modBus.register(RatlantisLogisticsGameTests.class);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRatFeed(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!(event.getTarget() instanceof Rat rat) || event.getLevel().isClientSide()) return;
        var held = event.getItemStack();
        ResourceLocation heldId = ForgeRegistries.ITEMS.getKey(held.getItem());
        if (heldId == null) return;
        if (heldId.equals(new ResourceLocation("rats", "cheese"))) {
            rat.heal(2.0F);
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
            return;
        }
        if (held.getItem() != RATLANTEAN_BAIT.get()) return;
        Player player = event.getEntity();
        if (!player.getAbilities().instabuild) held.shrink(1);
        rat.wildTrust += 10 + rat.getRandom().nextInt(10);
        rat.cheeseFeedings++;
        rat.heal(2.0F);
        if ((rat.wildTrust >= 100 && rat.getRandom().nextInt(3) == 0) || rat.cheeseFeedings >= 15) {
            var tamed = RatUtils.tameRat(rat, rat.level());
            tamed.tame(player);
            tamed.setCommand(com.github.alexthe666.rats.server.entity.rat.RatCommand.SIT);
            tamed.level().broadcastEntityEvent(tamed, (byte) 7);
        } else {
            rat.level().broadcastEntityEvent(rat, (byte) 6);
        }
        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);
    }
}
