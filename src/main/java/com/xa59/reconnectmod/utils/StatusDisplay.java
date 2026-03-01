package com.xa59.reconnectmod.utils;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;

public class StatusDisplay {
    private static boolean showOverlay = false;

    public static void sendOverlayMessageAfterJoin(String message, ChatFormatting formatting) {
        ClientPlayConnectionEvents.JOIN.register(new ClientPlayConnectionEvents.Join() {
            @Override
            public void onPlayReady(ClientPacketListener handler, PacketSender sender, Minecraft client) {
                if (!showOverlay && client.player != null) {
                    client.player.sendOverlayMessage(
                            Component.literal(message)
                                    .withStyle(formatting)
                    );
                    showOverlay = true;
                }
            }

        });
    }

    public static void resetOverlay() {
        showOverlay = false;
    }
}
