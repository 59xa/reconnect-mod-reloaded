package io.xa59.reconnect.utils;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class FabricStatusDisplay implements IStatusDisplay {
    private boolean showOverlay = false;

    @Override
    public void sendOverlayMessageAfterJoin(String message, ChatFormatting formatting) {
        ClientPlayConnectionEvents.JOIN.register((_, _, client) -> {
            if (!showOverlay && client.player != null) {
                client.player.sendOverlayMessage(
                        Component.literal(message).withStyle(formatting)
                );
                showOverlay = true;
            }
        });
    }

    @Override
    public void resetOverlay() {
        showOverlay = false;
    }
}