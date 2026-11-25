package io.github.blizzard39.carthelper.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class CartHelperClient implements ClientModInitializer {

    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("cart-helper", "main"));

    private static KeyMapping toggleKey;
    private static boolean enabled = false;
    private final CartLogic logic = new CartLogic();

    @Override
    public void onInitializeClient() {
        CartConfig.load();
        enabled = CartConfig.get().startEnabled;

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.cart-helper.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                enabled = !enabled;
                logic.reset();
                if (client.player != null && CartConfig.get().chatMessages) {
                    client.player.displayClientMessage(
                            Component.literal("Cart Helper: " + (enabled ? "ON" : "OFF")),
                            false
                    );
                }
            }
            if (enabled) {
                logic.tick(client);
            }
        });
    }
}
