package io.xa59.reconnect.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class StatusDisplay {
    public static void sendOverlayMessageAfterJoin(String message, ChatFormatting formatting) {
        // Does not work
        Minecraft instance = Minecraft.getInstance();
        if (instance.player != null) {
            instance.player.sendOverlayMessage(Component.literal(message).withStyle(formatting));
        }
    }

    public static void resetOverlay() {

    }
}
