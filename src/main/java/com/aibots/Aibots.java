package com.aibots;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aibots.bridge.BridgeClient;
import com.aibots.bridge.ContextCollector;
import com.aibots.entity.ModEntityTypes;

public class Aibots implements ModInitializer {
    public static final String MOD_ID = "aibots";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final String BRIDGE_URL = "http://127.0.0.1:8000/context";
    private static final long SEND_PERIOD_SECONDS = 5L;

    private BridgeClient bridgeClient;

    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");

        ModEntityTypes.registerAll();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            this.bridgeClient = new BridgeClient(
                BRIDGE_URL,
                () -> {
                    ServerPlayer player = firstPlayer(server);
                    return player != null ? ContextCollector.buildPayload(player, null) : null;
                },
                response -> LOGGER.info("[BridgeResponse] {}", response)
            );
            this.bridgeClient.start(SEND_PERIOD_SECONDS);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (this.bridgeClient != null) {
                this.bridgeClient.stop();
                this.bridgeClient = null;
            }
        });

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            if (sender instanceof ServerPlayer player && this.bridgeClient != null) {
                String chat = message.decoratedContent().getString();
                this.bridgeClient.sendCurrentPayload(() -> ContextCollector.buildPayload(player, chat));
            }
        });
    }

    private static ServerPlayer firstPlayer(net.minecraft.server.MinecraftServer server) {
        var players = server.getPlayerList().getPlayers();
        return players.isEmpty() ? null : players.get(0);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}