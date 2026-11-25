package io.github.blizzard39.carthelper.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CartConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("cart-helper.json");
    private static CartConfig instance = new CartConfig();

    // General
    public boolean startEnabled = false;
    public boolean chatMessages = true;
    public boolean switchBackToBow = false;

    // Rail types
    public boolean allowRail = true;
    public boolean allowPowered = true;
    public boolean allowDetector = true;
    public boolean allowActivator = true;

    // Times (all in ticks, 20 ticks = 1 second)
    public int windowTicks = 40;     // 10..100
    public int minDrawTicks = 3;     // 1..20
    public int railDelayTicks = 0;   // 0..10
    public int cartDelayTicks = 0;   // 0..10
    public int backDelayTicks = 0;   // 0..20
    public int cooldownTicks = 0;    // 0..40

    public static CartConfig get() {
        return instance;
    }

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                CartConfig loaded = GSON.fromJson(Files.readString(FILE), CartConfig.class);
                if (loaded != null) {
                    instance = loaded;
                }
            }
        } catch (Exception ignored) {
            // broken file: keep the defaults
        }
        instance.windowTicks = clamp(instance.windowTicks, 10, 100);
        instance.minDrawTicks = clamp(instance.minDrawTicks, 1, 20);
        instance.railDelayTicks = clamp(instance.railDelayTicks, 0, 10);
        instance.cartDelayTicks = clamp(instance.cartDelayTicks, 0, 10);
        instance.backDelayTicks = clamp(instance.backDelayTicks, 0, 20);
        instance.cooldownTicks = clamp(instance.cooldownTicks, 0, 40);
        save();
    }

    public static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(instance));
        } catch (IOException ignored) {
        }
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
