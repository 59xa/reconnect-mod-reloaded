package io.xa59.reconnect.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT, modid = "reconnect")
public class NeoForgeStatusDisplay implements IStatusDisplay {

    private boolean showOverlay = false;

    @Override
    public void sendOverlayMessageAfterJoin(String message, ChatFormatting formatting) {
        NeoForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
                if (!showOverlay && Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.sendOverlayMessage(
                            Component.literal(message).withStyle(formatting)
                    );
                    showOverlay = true;
                }
            }
        });
    }

    @Override
    public void resetOverlay() {
        showOverlay = false;
    }

}