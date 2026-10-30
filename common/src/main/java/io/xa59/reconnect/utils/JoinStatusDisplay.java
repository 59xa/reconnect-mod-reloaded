package io.xa59.reconnect.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class JoinStatusDisplay implements IStatusDisplay {
    private Component pendingMessage;
    private boolean shown;

    @Override
    public void sendOverlayMessageAfterJoin(String message, ChatFormatting formatting) {
        pendingMessage = Component.literal(message).withStyle(formatting);
    }

    @Override
    public void resetOverlay() {
        shown = false;
    }

    public void onJoin(Minecraft client) {
        if (!shown && pendingMessage != null && client.player != null) {
            ClientCompatibility.showOverlay(client.player, pendingMessage);
            pendingMessage = null;
            shown = true;
        }
    }
}
