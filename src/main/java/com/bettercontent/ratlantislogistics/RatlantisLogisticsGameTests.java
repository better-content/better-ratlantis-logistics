package com.bettercontent.ratlantislogistics;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@GameTestHolder(RatlantisLogistics.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RatlantisLogisticsGameTests {
    private RatlantisLogisticsGameTests() {}

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        event.register(RatlantisLogisticsGameTests.class);
    }

    @GameTest(templateNamespace = RatlantisLogistics.MOD_ID, template = "empty", timeoutTicks = 40)
    public static void runtimeIntegrationsLoad(GameTestHelper helper) {
        helper.assertTrue(ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation("rats", "rat")),
            "Rats runtime registry must be present");
        helper.assertTrue(ForgeRegistries.ITEMS.containsKey(new ResourceLocation("prettypipes", "pipe")),
            "Pretty Pipes runtime registry must be present");
        helper.assertTrue(ForgeRegistries.ITEMS.containsKey(new ResourceLocation(RatlantisLogistics.MOD_ID, "ratlantean_bait")),
            "Ratlantis Logistics items must be registered");
        helper.succeed();
    }
}
