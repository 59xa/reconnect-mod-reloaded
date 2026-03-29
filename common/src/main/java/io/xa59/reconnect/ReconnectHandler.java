package io.xa59.reconnect;

import com.mojang.realmsclient.RealmsMainScreen;
import io.xa59.reconnect.utils.ArgumentUtils;
import io.xa59.reconnect.utils.RealmsStateManager;
import io.xa59.reconnect.utils.StatusDisplay;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

public class ReconnectHandler {
    public static int reconnect(Minecraft instance) {
        Minecraft client = Minecraft.getInstance();
        ServerData currentServer = client.getCurrentServer();

        if (currentServer == null) {
            if (client.player != null) {
                client.player.sendOverlayMessage(
                        Component.literal("You are currently not connected to a multiplayer server.")
                                .withStyle(ChatFormatting.RED)
                );
            }
            return 0;
        }

        // Disconnect user
        if (client.level != null) {
            client.level.disconnect(Component.literal("[Reconnect] User requested reconnect sequence using /reconnect."));
        }

        Screen currentScreen = client.screen;
        assert currentScreen != null;
        client.disconnect(currentScreen, false);

        // If on Realms, handle connection through here instead
        if (currentServer.isRealm()) {
            if (RealmsStateManager.currentRealm != null) {
                client.execute(() -> {
                    RealmsMainScreen.play(RealmsStateManager.currentRealm, currentScreen);
                });

                sendSuccessMessage();
                return 1;
            } else {
                if (client.player != null) {
                    client.player.sendOverlayMessage(
                            Component.literal("Failed to fetch Realm data for reconnect.")
                                    .withStyle(ChatFormatting.RED)
                    );
                }
                return 0;
            }
        }

        // Parse current server address
        ServerAddress serverAddress = ServerAddress.parseString(currentServer.ip);

        client.execute(() -> {
            ConnectScreen.startConnecting(currentScreen, client, serverAddress, currentServer, true, null);
        });

        sendSuccessMessage();

        return 1;
    }

    private static void sendSuccessMessage() {
        StatusDisplay.resetOverlay();
        StatusDisplay.sendOverlayMessageAfterJoin("Successfully reconnected.", ChatFormatting.GREEN);
        StatusDisplay.resetOverlay();
    }

    public static void handlePostJoin(Minecraft client) {
        String commandToRun = ArgumentUtils.getPostCommand();
        int delayTime = ArgumentUtils.getDelaySeconds();

        if (commandToRun == null) return;

        // Clear shared state
        ArgumentUtils.clear();

        new Thread(() -> {
            try {
                Thread.sleep(delayTime * 1000L); // sleep() expects milliseconds, convert value
            } catch (InterruptedException ignored) {}

            client.execute(() -> {
                if (client.player != null) {
                    client.player.connection.sendCommand(commandToRun);
                }
            });
        }).start();
    }
}