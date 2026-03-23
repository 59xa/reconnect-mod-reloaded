package io.xa59.reconnect.utils;

import net.minecraft.ChatFormatting;

public interface IStatusDisplay {
    void sendOverlayMessageAfterJoin(String message, ChatFormatting formatting);
    void resetOverlay();
}
