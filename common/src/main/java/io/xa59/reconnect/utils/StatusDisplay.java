package io.xa59.reconnect.utils;

import net.minecraft.ChatFormatting;

public class StatusDisplay {

    private static IStatusDisplay impl;

    public static void setImplementation(IStatusDisplay implementation) {
        impl = implementation;
    }

    public static void sendOverlayMessageAfterJoin(String message, ChatFormatting formatting) {
        if (impl != null) {
            impl.sendOverlayMessageAfterJoin(message, formatting);
        }
    }

    public static void resetOverlay() {
        if (impl != null) {
            impl.resetOverlay();
        }
    }

}