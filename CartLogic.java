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

    private enum Phase { IDLE, WINDOW, PLACE_RAIL, PLACE_CART, BACK }

    private Phase phase = Phase.IDLE;
    private boolean wasUsingBow;
    private int lastDrawTicks;
    private int bowSlot = -1;
    private int comboBowSlot = -1;
    private int prevSlot = -1;
    private int windowLeft;
    private int delayLeft;
    private int cooldownLeft;
    private BlockPos railPos;

    public void reset() {
        phase = Phase.IDLE;
        wasUsingBow = false;
        lastDrawTicks = 0;
        bowSlot = -1;
        comboBowSlot = -1;
        prevSlot = -1;
        windowLeft = 0;
        delayLeft = 0;
        cooldownLeft = 0;
        railPos = null;
    }

    public void tick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.gameMode == null) {
            reset();
            return;
        }
        CartConfig cfg = CartConfig.get();
        int slot = player.getInventory().getSelectedSlot();

        if (cooldownLeft > 0) {
            cooldownLeft--;
        }

        // 1) Was the bow just fired?
        boolean usingBow = player.isUsingItem() && player.getUseItem().is(Items.BOW);
        if (usingBow) {
            lastDrawTicks = player.getTicksUsingItem();
            bowSlot = slot;
        } else if (wasUsingBow
                && (phase == Phase.IDLE || phase == Phase.WINDOW)
                && cooldownLeft == 0
                && slot == bowSlot
                && lastDrawTicks >= cfg.minDrawTicks
                && findHotbar(player, Items.TNT_MINECART) >= 0
                && hasAnyRail(player)) {
            phase = Phase.WINDOW;
            windowLeft = cfg.windowTicks;
            comboBowSlot = bowSlot;
        }
        wasUsingBow = usingBow;

        boolean holdingRail = isRail(player.getInventory().getItem(slot));
        boolean canAct = mc.screen == null && !player.isSpectator();

        // 2) The time window ticks down while we wait for / place the rail
        if (phase == Phase.WINDOW || phase == Phase.PLACE_RAIL) {
            if (windowLeft <= 0) {
                phase = Phase.IDLE;
            } else {
                windowLeft--;
            }
        }

        // 3) Player switched TO a rail slot inside the window
        if (phase == Phase.WINDOW && holdingRail && slot != prevSlot) {
            phase = Phase.PLACE_RAIL;
            delayLeft = cfg.railDelayTicks;
        }

        // 4) Place the rail (after the rail delay)
        if (phase == Phase.PLACE_RAIL) {
            if (!holdingRail) {
                phase = Phase.WINDOW;
            } else if (canAct) {
                if (delayLeft > 0) {
                    delayLeft--;
                } else {
                    BlockPos placed = tryPlaceRail(mc, player);
                    if (placed != null) {
                        railPos = placed;
                        phase = Phase.PLACE_CART;
                        delayLeft = cfg.cartDelayTicks;
                    }
                }
            }
        }

        // 5) Place the TNT minecart (after the cart delay)
        if (phase == Phase.PLACE_CART) {
            if (delayLeft > 0) {
                delayLeft--;
            } else {
                tryPlaceCart(mc, player, railPos);
                if (cfg.switchBackToBow) {
                    phase = Phase.BACK;
                    delayLeft = cfg.backDelayTicks;
                } else {
                    finish();
                }
            }
        }

        // 6) Switch back to the bow (after the back delay)
        if (phase == Phase.BACK) {
            if (delayLeft > 0) {
                delayLeft--;
            } else {
                if (comboBowSlot >= 0 && player.getInventory().getItem(comboBowSlot).is(Items.BOW)) {
                    player.getInventory().setSelectedSlot(comboBowSlot);
                }
                finish();
            }
        }

        prevSlot = player.getInventory().getSelectedSlot();
    }

    private void finish() {
        phase = Phase.IDLE;
        railPos = null;
        cooldownLeft = CartConfig.get().cooldownTicks;
    }

    /** Places the rail where the crosshair points. Returns the rail position, or null if nothing was placed. */
    private BlockPos tryPlaceRail(Minecraft mc, LocalPlayer player) {
        if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        if (findHotbar(player, Items.TNT_MINECART) < 0) {
            return null; // never place a rail if there is no cart to follow it
        }
        if (!(player.getMainHandItem().getItem() instanceof BlockItem railItem)) {
            return null;
        }

        BlockPos clicked = hit.getBlockPos();
        BlockPos pos = mc.level.getBlockState(clicked).canBeReplaced()
                ? clicked
                : clicked.relative(hit.getDirection());
        if (!mc.level.getBlockState(pos).canBeReplaced()) {
            return null;
        }
        BlockState railState = railItem.getBlock().defaultBlockState();
        if (!railState.canSurvive(mc.level, pos)) {
            return null;
        }

        InteractionResult placed = mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
        if (!placed.consumesAction()) {
            return null;
        }
        player.swing(InteractionHand.MAIN_HAND);
        return pos;
    }

    private void tryPlaceCart(Minecraft mc, LocalPlayer player, BlockPos pos) {
        if (pos == null || !(mc.level.getBlockState(pos).getBlock() instanceof BaseRailBlock)) {
            return;
        }
        int cartSlot = findHotbar(player, Items.TNT_MINECART);
        if (cartSlot < 0) {
            return;
        }
        player.getInventory().setSelectedSlot(cartSlot);
        BlockHitResult cartHit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        InteractionResult result = mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, cartHit);
        if (result.consumesAction()) {
            player.swing(InteractionHand.MAIN_HAND);
        }
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
        CartConfig c = CartConfig.get();
        return (c.allowRail && stack.is(Items.RAIL))
                || (c.allowPowered && stack.is(Items.POWERED_RAIL))
                || (c.allowDetector && stack.is(Items.DETECTOR_RAIL))
                || (c.allowActivator && stack.is(Items.ACTIVATOR_RAIL));
    }
}
