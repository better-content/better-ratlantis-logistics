package com.bettercontent.ratlantislogistics;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CraftingGateTest {
    @Test void tiersMatchThePublishedLogisticsLadder() {
        assertEquals("BULK", CraftingGate.tier(new ResourceLocation("prettypipes", "pipe")).name());
        assertEquals("INTERMEDIATE", CraftingGate.tier(new ResourceLocation("prettypipes", "medium_speed_module")).name());
        assertEquals("RARE", CraftingGate.tier(new ResourceLocation("prettypipes", "high_speed_module")).name());
        assertEquals("RARE", CraftingGate.tier(new ResourceLocation("ae2", "controller")).name());
        assertNull(CraftingGate.tier(new ResourceLocation("ae2", "fluix_glass_cable")));
        assertNull(CraftingGate.tier(new ResourceLocation("create", "belt_connector")));
    }
}
