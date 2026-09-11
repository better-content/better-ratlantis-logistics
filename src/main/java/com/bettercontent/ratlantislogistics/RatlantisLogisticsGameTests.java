package com.bettercontent.ratlantislogistics;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.github.alexthe666.rats.RatConfig;
import com.github.alexthe666.rats.registry.RatsEntityRegistry;
import com.github.alexthe666.rats.server.entity.ai.goal.harvest.RatHarvestCropsGoal;
import com.github.alexthe666.rats.server.entity.rat.Rat;
import com.github.alexthe666.rats.server.entity.rat.RatCommand;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import java.util.UUID;

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

    @GameTest(templateNamespace = RatlantisLogistics.MOD_ID, template = "empty", timeoutTicks = 40)
    public static void baitConsumesOneAndBuildsTrust(GameTestHelper helper) {
        Rat rat = rat(helper);
        ServerPlayer player = player(helper);
        try {
            var bait = new ItemStack(RatlantisLogistics.RATLANTEAN_BAIT.get(), 3);
            var event = feed(player, rat, bait);
            helper.assertTrue(event.isCanceled() && event.getCancellationResult() == InteractionResult.CONSUME,
                "Bait event must be consumed by the registered handler");
            helper.assertTrue(bait.getCount() == 2 && rat.cheeseFeedings == 1,
                "Survival feeding must consume exactly one bait and count one feeding");
            helper.assertTrue(rat.wildTrust >= 10 && rat.wildTrust <= 19, "Bait trust must increase within its contract");
            player.getAbilities().instabuild = true;
            feed(player, rat, bait);
            helper.assertTrue(bait.getCount() == 2 && rat.cheeseFeedings == 2,
                "Creative feeding must retain bait while counting the action");
        } finally { rat.discard(); }
        helper.succeed();
    }

    @GameTest(templateNamespace = RatlantisLogistics.MOD_ID, template = "empty", timeoutTicks = 40)
    public static void fifteenthBaitTamesTheRatForItsFeeder(GameTestHelper helper) {
        Rat rat = rat(helper);
        ServerPlayer player = player(helper);
        rat.cheeseFeedings = 14;
        rat.wildTrust = 0; // Force the guaranteed-count path, independently of random trust taming.
        var position = rat.blockPosition();
        try {
            var bait = new ItemStack(RatlantisLogistics.RATLANTEAN_BAIT.get(), 2);
            feed(player, rat, bait);
            var tamed = helper.getLevel().getEntitiesOfClass(TamedRat.class, new AABB(position).inflate(1),
                entity -> player.getUUID().equals(entity.getOwnerUUID()));
            helper.assertTrue(rat.isRemoved() && tamed.size() == 1, "Fifteenth bait must replace the wild rat exactly once");
            helper.assertTrue(tamed.get(0).isTame() && tamed.get(0).getCommand() == RatCommand.SIT,
                "Tamed rat must belong to the feeder and sit");
            helper.assertTrue(bait.getCount() == 1, "Guaranteed tame must still consume one bait");
            tamed.forEach(TamedRat::discard);
        } finally { rat.discard(); }
        helper.succeed();
    }

    @GameTest(templateNamespace = RatlantisLogistics.MOD_ID, template = "empty", timeoutTicks = 40)
    public static void cheeseAndUnrelatedItemsCannotBuildTrust(GameTestHelper helper) {
        Rat rat = rat(helper);
        ServerPlayer player = player(helper);
        try {
            var cheese = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("rats", "cheese")), 2);
            helper.assertTrue(feed(player, rat, cheese).isCanceled(), "Cheese must be intercepted before upstream taming");
            var stone = new ItemStack(Items.COBBLESTONE, 2);
            helper.assertTrue(!feed(player, rat, stone).isCanceled(), "Unrelated item must not be handled as bait");
            helper.assertTrue(stone.getCount() == 2 && rat.wildTrust == 0 && rat.cheeseFeedings == 0,
                "Non-bait items must not build trust or feeding progress");
        } finally { rat.discard(); }
        helper.succeed();
    }

    @GameTest(templateNamespace = RatlantisLogistics.MOD_ID, template = "farm", timeoutTicks = 40)
    public static void cropGoalReplantsOnlyWithEligibleSeed(GameTestHelper helper) {
        harvest(helper, true);
        helper.succeed();
    }

    @GameTest(templateNamespace = RatlantisLogistics.MOD_ID, template = "farm", timeoutTicks = 40)
    public static void cropGoalLeavesNoFreeReplantWithoutSeed(GameTestHelper helper) {
        harvest(helper, false);
        helper.succeed();
    }

    @GameTest(templateNamespace = RatlantisLogistics.MOD_ID, template = "farm", timeoutTicks = 40)
    public static void replantingSelectsNearestEligibleInput(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
        var near = drop(helper, pos, new ItemStack(Items.WHEAT_SEEDS, 3), 0.1);
        var far = drop(helper, pos, new ItemStack(Items.WHEAT_SEEDS, 2), 1.1);
        var wrong = drop(helper, pos, new ItemStack(Items.COBBLESTONE, 3), 0.0);
        try {
            helper.assertTrue(ReplantingInputs.consumeCropSeed(helper.getLevel(), pos), "Eligible input must be found");
            helper.assertTrue(near.getItem().getCount() == 2 && far.getItem().getCount() == 2 && wrong.getItem().getCount() == 3,
                "Consume only one seed from the nearest eligible stack");
            near.discard(); far.discard();
            helper.assertTrue(!ReplantingInputs.consumeCropSeed(helper.getLevel(), pos), "An unrelated item cannot pay for replanting");
        } finally { near.discard(); far.discard(); wrong.discard(); }
        helper.succeed();
    }

    private static void harvest(GameTestHelper helper, boolean supplySeed) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
        helper.getLevel().setBlockAndUpdate(pos.below(), Blocks.FARMLAND.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7));
        var seed = supplySeed ? drop(helper, pos, new ItemStack(Items.WHEAT_SEEDS, 1), 0.0) : null;
        helper.assertTrue(new ItemStack(Items.WHEAT_SEEDS).is(TagKey.create(Registries.ITEM,
            new ResourceLocation("bumblezone_cultivars", "seeds"))), "GameTest-only eligible seed tag must be loaded");
        TamedRat rat = RatsEntityRegistry.TAMED_RAT.get().create(helper.getLevel());
        rat.setNoAi(true);
        rat.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        helper.getLevel().addFreshEntity(rat);
        boolean oldBreak = RatConfig.ratsBreakBlockOnHarvest;
        var drops = helper.getLevel().getGameRules().getRule(GameRules.RULE_DOBLOCKDROPS);
        boolean oldDrops = drops.get();
        try {
            RatConfig.ratsBreakBlockOnHarvest = false;
            drops.set(false, helper.getLevel().getServer()); // Prevent harvested drops from supplying the test's payment.
            var goal = new RatHarvestCropsGoal(rat);
            goal.setTargetBlock(pos);
            goal.tick(); // Exercise the transformed upstream method and our actual mixin redirect.
            var result = helper.getLevel().getBlockState(pos);
            helper.assertTrue(supplySeed ? result.is(Blocks.WHEAT) && result.getValue(CropBlock.AGE) == 0 : result.isAir(),
                "Harvest must replant only when an eligible input is consumed");
            if (seed != null) helper.assertTrue(seed.isRemoved(), "One-item payment must be consumed completely");
        } finally {
            RatConfig.ratsBreakBlockOnHarvest = oldBreak;
            drops.set(oldDrops, helper.getLevel().getServer());
            rat.discard();
            if (seed != null) seed.discard();
        }
    }

    private static ItemEntity drop(GameTestHelper helper, BlockPos pos, ItemStack stack, double offset) {
        var entity = new ItemEntity(helper.getLevel(), pos.getX() + 0.5 + offset, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        entity.setNoGravity(true);
        entity.setDeltaMovement(Vec3.ZERO);
        helper.getLevel().addFreshEntity(entity);
        return entity;
    }

    private static Rat rat(GameTestHelper helper) {
        Rat rat = RatsEntityRegistry.RAT.get().create(helper.getLevel());
        var pos = helper.absolutePos(BlockPos.ZERO);
        rat.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        rat.setNoAi(true);
        helper.getLevel().addFreshEntity(rat);
        return rat;
    }

    private static ServerPlayer player(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "rat-test"));
        player.getAbilities().instabuild = false;
        return player;
    }

    private static PlayerInteractEvent.EntityInteractSpecific feed(ServerPlayer player, Rat rat, ItemStack held) {
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        var event = new PlayerInteractEvent.EntityInteractSpecific(player, InteractionHand.MAIN_HAND, rat, Vec3.ZERO);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }
}
