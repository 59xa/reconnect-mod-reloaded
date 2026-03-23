package io.xa59.reconnect;

import io.xa59.reconnect.utils.StatusDisplay;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

public class ReconnectCommands {
    public static int reconnect(Minecraft instance) {
        Minecraft client = Minecraft.getInstance();
        ServerData currentServer = client.getCurrentServer();

        if (currentServer == null) {
            if (client.player != null) {
                client.player.sendSystemMessage(
                        Component.literal("You are currently not connected to a multiplayer server.")
                                .withStyle(ChatFormatting.RED)
                );

                return 0;
            }
        }

        assert currentServer != null;
        if (currentServer.isRealm()) {
            assert client.player != null;
            client.player.sendSystemMessage(
                    Component.literal("Reconnecting in Realms is not supported.")
                            .withStyle(ChatFormatting.RED)
            );
        }

        // Parse current server address
        ServerAddress serverAddress = ServerAddress.parseString(currentServer.ip);

        // Disconnect user
        if (client.level != null) {
            client.level.disconnect(Component.literal("[Reconnect]: User requested reconnect sequence using /reconnect."));
        }

        assert client.screen != null;
        client.disconnect(client.screen, false);

        client.execute(() -> {
            ConnectScreen.startConnecting(null, client, serverAddress, currentServer, true, null);
        });

        StatusDisplay.resetOverlay();
        StatusDisplay.sendOverlayMessageAfterJoin("Successfully reconnected.", ChatFormatting.GREEN);
        StatusDisplay.resetOverlay();

        return 1;
    }
}
