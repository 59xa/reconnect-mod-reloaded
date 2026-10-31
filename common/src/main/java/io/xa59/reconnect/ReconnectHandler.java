package io.xa59.reconnect;

import io.xa59.reconnect.utils.ArgumentUtils;
import io.xa59.reconnect.utils.ClientCompatibility;
import io.xa59.reconnect.utils.RealmsStateManager;
import io.xa59.reconnect.utils.StatusDisplay;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

public class ReconnectHandler {

    // Flag to track if a reconnect is actually in progress and prevent multiple event fires
    private static boolean isReconnecting = false;

    public static int reconnect() {
        Minecraft client = Minecraft.getInstance();
        ServerData currentServer = client.getCurrentServer();

        if (currentServer == null) {
            if (client.player != null) {
                ClientCompatibility.showOverlay(client.player,
                        Component.literal("You are currently not connected to a multiplayer server.")
                                .withStyle(ChatFormatting.RED)
                );
            }
            return 0;
        }

        // Mark that an intentional reconnect sequence has started
        isReconnecting = true;
        boolean onRealm = ClientCompatibility.isRealm(client, currentServer);

        // Disconnect user
        if (client.level != null) {
            ClientCompatibility.disconnectLevel(client, Component.literal("[Reconnect] User requested reconnect sequence using /reconnect."));
        }

        Screen currentScreen = ClientCompatibility.currentScreen(client);
        assert currentScreen != null;
        ClientCompatibility.disconnectClient(client, currentScreen);

        // If on Realms, handle connection through here instead
        if (onRealm) {
            if (RealmsStateManager.currentRealm != null) {
                client.execute(() -> ClientCompatibility.playRealm(RealmsStateManager.currentRealm, currentScreen));

                if (ArgumentUtils.getPostCommand() == null) sendSuccessMessage();

                return 1;
            } else {
                isReconnecting = false; // Abort reconnect state if fetching fails
                if (client.player != null) {
                    ClientCompatibility.showOverlay(client.player,
                            Component.literal("Failed to fetch Realm data for reconnect.")
                                    .withStyle(ChatFormatting.RED)
                    );
                }
                return 0;
            }
        }

        // Parse current server address
        ServerAddress serverAddress = ServerAddress.parseString(currentServer.ip);

        client.execute(() -> ClientCompatibility.connect(client, currentScreen, serverAddress, currentServer));

        if (ArgumentUtils.getPostCommand() == null) sendSuccessMessage();

        return 1;
    }

    private static void sendSuccessMessage() {
        StatusDisplay.resetOverlay();
        StatusDisplay.sendOverlayMessageAfterJoin("Successfully reconnected.", ChatFormatting.GREEN);
        StatusDisplay.resetOverlay();
    }

    public static void handlePostJoin(Minecraft client) {
        AutoReconnectHandler.connected(client);
        // Abort if not triggered by command, or if it already ran
        if (!isReconnecting) {
            return;
        }

        // Consume the intent so this logic only executes exactly once
        isReconnecting = false;

        String commandToRun = ArgumentUtils.getPostCommand();
        int delayTime = ArgumentUtils.getDelaySeconds();

        // Clear shared state
        ArgumentUtils.clear();

        assert client.player != null;

        // If null or empty
        if (commandToRun == null || commandToRun.trim().isEmpty()) {
            sendSuccessMessage();
            return;
        }

        // If command exists with delay, display intent overlay message
        if (delayTime != 0) {
            ClientCompatibility.showOverlay(client.player,
                    Component.literal("Successfully reconnected. Running command payload after " + delayTime + " seconds.")
                            .withStyle(ChatFormatting.YELLOW)
            );
        }

        new Thread(() -> {
            try {
                Thread.sleep(Math.max(200, delayTime * 1000L));
            } catch (InterruptedException ignored) {}

            client.execute(() -> {
                if (client.player != null) {
                    client.player.connection.sendCommand(commandToRun);

                    ClientCompatibility.showOverlay(client.player,
                            Component.literal("Command execution complete.")
                                    .withStyle(ChatFormatting.GREEN)
                    );
                }
            });
        }).start();
    }

}
