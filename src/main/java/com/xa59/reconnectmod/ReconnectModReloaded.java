package com.xa59.reconnectmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReconnectModReloaded implements ClientModInitializer {
	public static final String MOD_ID = "reconnect-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";

	@Override
	public void onInitializeClient() {
		LOGGER.info(ANSI_GREEN + "reconnect-mod" + ANSI_YELLOW + ": successfully initialised.");

		// Register the /reconnect command
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
					ClientCommands.literal("reconnect")
							.executes(context -> {
								Minecraft client = Minecraft.getInstance();
								ServerData currentServer = client.getCurrentServer();

								// Check if the current server is either singleplayer or multiplayer
								if (currentServer == null) {
                                    if (client.player != null) {
                                        client.player.sendOverlayMessage(Component.nullToEmpty("§cYou are currently not connected to a multiplayer server."));
                                    }
                                    return 0;
								}

								if (currentServer.isRealm()) {
									client.player.sendOverlayMessage(Component.nullToEmpty("§cDue to the Realms API being internal, you can not use the /reconnect command."));
									return 0;
								}

								// Send feedback to player
								context.getSource().sendFeedback(Component.nullToEmpty("RM-R: /reconnect command called."));

								// Parse current server address
								ServerAddress serverAddress = ServerAddress.parseString(currentServer.ip);

								// Disconnect user
                                if (client.level != null) {
                                    client.level.disconnect(Component.nullToEmpty("RM-R: User requested reconnect sequence using /reconnect."));
                                }
                                client.disconnect(client.screen, false);

								// Initiate reconnect sequence
								client.execute(() -> {
									ConnectScreen.startConnecting(null, client, serverAddress, currentServer, true, null);
								});

								context.getSource().sendFeedback(Component.nullToEmpty("RM-R: Successfully reconnected."));
								return 1;
							})
			);
		});
	}
}
