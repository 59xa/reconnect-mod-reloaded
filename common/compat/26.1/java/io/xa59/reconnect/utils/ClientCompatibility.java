package io.xa59.reconnect.utils;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public final class ClientCompatibility {
    public static void disconnectClient(Minecraft client, Screen screen) {
        client.disconnect(screen, false);
    }

    public static boolean isRealm(Minecraft client, ServerData server) {
        return server.isRealm();
    }

    public static void connect(Minecraft client, Screen screen, ServerAddress address, ServerData server) {
        ConnectScreen.startConnecting(screen, client, address, server, true, null);
    }

    public static void playRealm(RealmsServer server, Screen screen) {
        RealmsMainScreen.play(server, screen);
    }

    public static void disconnectLevel(Minecraft client, Component reason) {
        client.level.disconnect(reason);
    }

    public static Screen currentScreen(Minecraft client) {
        return client.screen;
    }

    public static void showOverlay(LocalPlayer player, Component message) {
        player.sendOverlayMessage(message);
    }
}
