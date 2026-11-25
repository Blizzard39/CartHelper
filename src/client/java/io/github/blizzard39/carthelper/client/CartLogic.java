package io.github.blizzard39.carthelper.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CartLogic {

    private static final int WINDOW_TICKS = 40;   // 2 seconds
    private static final int MIN_DRAW_TICKS = 3;  // shorter draws do not fire an arrow

    private boolean wasUsingBow;
    private int lastDrawTicks;
    private int bowSlot = -1;
    private int prevSlot = -1;
    private int windowLeft;
    private boolean armed;

    public void reset() {
        wasUsingBow = false;
        lastDrawTicks = 0;
        bowSlot = -1;
        prevSlot = -1;
        windowLeft = 0;
        armed = false;
    }

    public void tick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.gameMode == null) {
            reset();
            return;
        }
        int slot = player.getInventory().getSelectedSlot();

        // 1) Was the bow just fired?
        boolean usingBow = player.isUsingItem() && player.getUseItem().is(Items.BOW);
        if (usingBow) {
            lastDrawTicks = player.getTicksUsingItem();
            bowSlot = slot;
        } else if (wasUsingBow && slot == bowSlot && lastDrawTicks >= MIN_DRAW_TICKS) {
            if (findHotbar(player, Items.TNT_MINECART) >= 0 && hasAnyRail(player)) {
                windowLeft = WINDOW_TICKS;
                armed = false;
            }
        }
        wasUsingBow = usingBow;

        // 2) Inside the 2 second window
        if (windowLeft > 0) {
            windowLeft--;
            boolean holdingRail = isRail(player.getInventory().getItem(slot));
            if (!holdingRail) {
                armed = false;
            } else if (slot != prevSlot) {
                armed = true; // player just switched TO a rail slot
            }
            if (armed && mc.screen == null && !player.isSpectator() && tryPlace(mc, player)) {
                windowLeft = 0;
                armed = false;
            }
        } else {
            armed = false;
        }
        prevSlot = slot;
    }

    private boolean tryPlace(Minecraft mc, LocalPlayer player) {
        if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        int cartSlot = findHotbar(player, Items.TNT_MINECART);
        if (cartSlot < 0) {
            return false;
        }
        if (!(player.getMainHandItem().getItem() instanceof BlockItem railItem)) {
            return false;
        }

        BlockPos clicked = hit.getBlockPos();
        BlockPos railPos = mc.level.getBlockState(clicked).canBeReplaced()
                ? clicked
                : clicked.relative(hit.getDirection());
        if (!mc.level.getBlockState(railPos).canBeReplaced()) {
            return false;
        }
        BlockState railState = railItem.getBlock().defaultBlockState();
        if (!railState.canSurvive(mc.level, railPos)) {
            return false;
        }

        // Place the rail
        InteractionResult placed = mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
        if (!placed.consumesAction()) {
            return false;
        }
        player.swing(InteractionHand.MAIN_HAND);
        if (!(mc.level.getBlockState(railPos).getBlock() instanceof BaseRailBlock)) {
            return true; // unexpected result: stop here instead of retrying
        }

        // Switch to the TNT minecart and place it on the new rail
        player.getInventory().setSelectedSlot(cartSlot);
        BlockHitResult cartHit = new BlockHitResult(Vec3.atCenterOf(railPos), Direction.UP, railPos, false);
        InteractionResult cart = mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, cartHit);
        if (cart.consumesAction()) {
            player.swing(InteractionHand.MAIN_HAND);
        }
        return true;
    }

    private static int findHotbar(LocalPlayer player, Item item) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i).is(item)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean hasAnyRail(LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            if (isRail(player.getInventory().getItem(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRail(ItemStack stack) {
        return stack.is(Items.RAIL) || stack.is(Items.POWERED_RAIL)
                || stack.is(Items.DETECTOR_RAIL) || stack.is(Items.ACTIVATOR_RAIL);
    }
}
