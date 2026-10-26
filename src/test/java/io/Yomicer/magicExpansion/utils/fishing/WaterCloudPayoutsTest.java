package io.Yomicer.magicExpansion.utils.fishing;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

class WaterCloudPayoutsTest {

    @BeforeEach
    void setup() {
        MockBukkit.mock();
    }

    @AfterEach
    void cleanup() {
        MockBukkit.unmock();
    }

    @Test
    void doublingPreservesEntireStacksAndIndependentIdentity() {
        ItemStack reward = new ItemStack(Material.COBBLESTONE, 40);
        List<ItemStack> payouts = WaterCloudPayouts.copies(reward, true);
        assertEquals(2, payouts.size());
        assertEquals(80, payouts.stream().mapToInt(ItemStack::getAmount).sum(),
                "Two full stacks must be given rather than capping to 64");
        assertNotSame(payouts.get(0), payouts.get(1));
        payouts.get(0).setAmount(1);
        assertEquals(40, reward.getAmount());
        assertEquals(40, payouts.get(1).getAmount());
    }

    @Test
    void unstackableFishAreTwoDistinctItems() {
        ItemStack fish = new ItemStack(Material.COD_BUCKET, 1);
        var payouts = WaterCloudPayouts.copies(fish, true);
        assertEquals(2, payouts.size());
        assertEquals(Material.COD_BUCKET, payouts.get(0).getType());
        assertEquals(Material.COD_BUCKET, payouts.get(1).getType());
        assertNotSame(payouts.get(0), payouts.get(1));
        assertEquals(1, fish.getAmount(), "Doubling cannot modify the source fish");
    }

    @Test
    void regularAndInvalidPayoutsAreSafe() {
        ItemStack reward = new ItemStack(Material.COD, 2);
        var regular = WaterCloudPayouts.copies(reward, false);
        assertEquals(1, regular.size());
        assertNotSame(reward, regular.get(0));
        assertEquals(2, regular.get(0).getAmount());
        assertTrue(WaterCloudPayouts.copies(null, true).isEmpty());
        assertTrue(WaterCloudPayouts.copies(new ItemStack(Material.AIR), true).isEmpty());
    }
}
