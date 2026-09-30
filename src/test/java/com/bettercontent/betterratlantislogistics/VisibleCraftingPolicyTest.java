package com.bettercontent.betterratlantislogistics;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

final class VisibleCraftingPolicyTest {
    @Test
    void craftingDoesNotRemoveHiddenInventoryComponents() throws IOException {
        String entrypoint = Files.readString(Path.of(
                "src/main/java/com/bettercontent/betterratlantislogistics/RatlantisLogistics.java"));
        assertFalse(entrypoint.contains("CraftingGate"));
        assertFalse(entrypoint.contains("ItemCraftedEvent"));
    }
}
