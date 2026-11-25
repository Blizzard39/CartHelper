package io.github.blizzard39.carthelper.client;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

public class CartConfigScreen extends Screen {

    private final Screen parent;

    public CartConfigScreen(Screen parent) {
        super(Component.literal("Cart Helper Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        CartConfig c = CartConfig.get();
        int colW = 150;
        int gap = 10;
        int left = this.width / 2 - colW - gap / 2;
        int right = this.width / 2 + gap / 2;
        int y = 28;
        int step = 22;

        StringWidget titleWidget = new StringWidget(this.title, this.font);
        titleWidget.setPosition(this.width / 2 - titleWidget.getWidth() / 2, 10);
        addRenderableWidget(titleWidget);

        toggle(left, y, colW, "Start enabled", c.startEnabled, v -> c.startEnabled = v);
        toggle(right, y, colW, "Chat messages", c.chatMessages, v -> c.chatMessages = v);
        y += step;

        slider(left, y, colW, "Window", 10, 100, c.windowTicks,
                t -> String.format("%.1f s", t / 20.0), v -> c.windowTicks = v);
        slider(right, y, colW, "Min draw", 1, 20, c.minDrawTicks,
                CartConfigScreen::ticks, v -> c.minDrawTicks = v);
        y += step;

        slider(left, y, colW, "Rail delay", 0, 10, c.railDelayTicks,
                CartConfigScreen::ticks, v -> c.railDelayTicks = v);
        slider(right, y, colW, "Cart delay", 0, 10, c.cartDelayTicks,
                CartConfigScreen::ticks, v -> c.cartDelayTicks = v);
        y += step;

        slider(left, y, colW, "Back delay", 0, 20, c.backDelayTicks,
                CartConfigScreen::ticks, v -> c.backDelayTicks = v);
        slider(right, y, colW, "Cooldown", 0, 40, c.cooldownTicks,
                CartConfigScreen::ticks, v -> c.cooldownTicks = v);
        y += step;

        toggle(left, y, colW, "Rail", c.allowRail, v -> c.allowRail = v);
        toggle(right, y, colW, "Powered rail", c.allowPowered, v -> c.allowPowered = v);
        y += step;

        toggle(left, y, colW, "Detector rail", c.allowDetector, v -> c.allowDetector = v);
        toggle(right, y, colW, "Activator rail", c.allowActivator, v -> c.allowActivator = v);
        y += step;

        toggle(left, y, colW, "Back to bow", c.switchBackToBow, v -> c.switchBackToBow = v);
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                .bounds(right, y, colW, 20).build());
    }

    private void toggle(int x, int y, int w, String label, boolean value, Consumer<Boolean> setter) {
        addRenderableWidget(CycleButton.onOffBuilder(value)
                .create(x, y, w, 20, Component.literal(label), (button, v) -> setter.accept(v)));
    }

    private void slider(int x, int y, int w, String label, int min, int max, int current,
                        IntFunction<String> fmt, IntConsumer setter) {
        addRenderableWidget(new IntSlider(x, y, w, 20, label, min, max, current, fmt, setter));
    }

    private static String ticks(int t) {
        return t == 0 ? "instant" : t + "t (" + (t * 50) + "ms)";
    }

    @Override
    public void onClose() {
        CartConfig.save();
        this.minecraft.setScreen(parent);
    }

    private static class IntSlider extends AbstractSliderButton {
        private final String label;
        private final int min;
        private final int max;
        private final IntFunction<String> fmt;
        private final IntConsumer setter;

        IntSlider(int x, int y, int w, int h, String label, int min, int max, int current,
                  IntFunction<String> fmt, IntConsumer setter) {
            super(x, y, w, h, Component.empty(), (double) (current - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.fmt = fmt;
            this.setter = setter;
            updateMessage();
        }

        private int current() {
            return min + (int) Math.round(this.value * (max - min));
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(label + ": " + fmt.apply(current())));
        }

        @Override
        protected void applyValue() {
            setter.accept(current());
        }
    }
}
