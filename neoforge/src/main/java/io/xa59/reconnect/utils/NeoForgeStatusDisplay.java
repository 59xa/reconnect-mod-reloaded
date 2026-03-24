package io.xa59.reconnect.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = "reconnect")
public class NeoForgeStatusDisplay implements IStatusDisplay {

    private static String pendingMessage = null;
    private static ChatFormatting pendingFormatting = ChatFormatting.WHITE;

    @Override
    public void sendOverlayMessageAfterJoin(String message, ChatFormatting formatting) {
        pendingMessage = message;
        pendingFormatting = formatting;
    }

    @Override
    public void resetOverlay() {
        pendingMessage = null;
    }

    @SubscribeEvent
    public static void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        if (pendingMessage != null) {
            Minecraft mc = Minecraft.getInstance();

            if (mc.player != null) {
                mc.player.sendOverlayMessage(
                        Component.literal(pendingMessage).withStyle(pendingFormatting)
                );

                pendingMessage = null;
            }
        }
    }
}