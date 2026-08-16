package com.aibots.bridge;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.AABB;

/**
 * Builds the "(4.1) Java -> Python Context State Payload" JSON for a player.
 */
public final class ContextCollector {
    public static final long PERIOD_MS = 5000L;

    private ContextCollector() {
    }

    /**
     * Build a context payload for the given player. If {@code chatMessage} is null,
     * the trigger is PERIODIC_TICK; otherwise CHAT_MESSAGE.
     */
    public static String buildPayload(Player player, String chatMessage) {
        JsonObject root = new JsonObject();
        root.addProperty("timestamp", System.currentTimeMillis() / 1000L);
        root.addProperty("trigger", chatMessage != null ? "CHAT_MESSAGE" : "PERIODIC_TICK");
        root.add("player", buildPlayer(player));
        root.add("environment", buildEnvironment(player));
        if (chatMessage != null) {
            root.addProperty("chat_message", chatMessage);
        }
        return root.toString();
    }

    private static JsonObject buildPlayer(Player player) {
        JsonObject p = new JsonObject();
        p.addProperty("name", player.getGameProfile().getName());
        p.addProperty("hp", (int) Math.floor(player.getHealth()));
        p.addProperty("max_hp", (int) Math.floor(player.getMaxHealth()));
        p.addProperty("food_level", player.getFoodData().getFoodLevel());

        ItemStack main = player.getMainHandItem();
        p.addProperty("main_hand_item", main.getItem().getName(main).getString());
        return p;
    }

    private static JsonObject buildEnvironment(Player player) {
        JsonObject env = new JsonObject();

        if (player.level() instanceof ServerLevel serverLevel) {
            env.addProperty("dimension", serverLevel.dimension().location().toString());
            Holder<Biome> biomeHolder = serverLevel.getBiome(player.blockPosition());
            env.addProperty("biome", biomeHolder.unwrapKey().map(k -> k.location().toString()).orElse("unknown"));

            long dayTime = serverLevel.getDayTime() % 24000L;
            String timeOfDay = dayTime < 13000L ? "day" : "night";
            env.addProperty("time_of_day", timeOfDay);
            env.addProperty("light_level", serverLevel.getMaxLocalRawBrightness(player.blockPosition()));
        }

        JsonArray mobs = new JsonArray();
        for (LivingEntity near : findNearbyMobs(player)) {
            JsonObject mob = new JsonObject();
            mob.addProperty("type", near.getType().getDescriptionId());
            mob.addProperty("distance", Math.round(player.distanceTo(near) * 10.0) / 10.0);
            mobs.add(mob);
        }
        env.add("nearby_mobs", mobs);
        return env;
    }

    private static List<LivingEntity> findNearbyMobs(Player player) {
        List<LivingEntity> result = new ArrayList<>();
        int radius = 24;
        if (player.level() instanceof ServerLevel serverLevel) {
            AABB box = player.getBoundingBox().inflate(radius);
            for (Mob mob : serverLevel.getEntitiesOfClass(Mob.class, box)) {
                if (!mob.is(player)) {
                    result.add(mob);
                }
            }
        }
        return result;
    }
}