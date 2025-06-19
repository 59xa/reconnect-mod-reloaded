package com.meganpark.reconnectmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
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
		LOGGER.info(ANSI_GREEN + "Reconnect Mod Reloaded " + ANSI_YELLOW + "SUCCESSFULLY INITIALIZED!");

		// Register the /reconnect command
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
					ClientCommandManager.literal("reconnect")
							.executes(context -> {
								MinecraftClient client = MinecraftClient.getInstance();
								ServerInfo currentServer = client.getCurrentServerEntry();

								// Check if the current server is either singleplayer or multiplayer
								if (currentServer == null) {
                                    if (client.player != null) {
                                        client.player.sendMessage(Text.of("RM-R: You are currently not connected to a multiplayer server."), false);
                                    }
                                    return 0;
								}

								if (currentServer.isRealm()) {
									context.getSource().sendFeedback(Text.of("RM-R: Due to the Realms API being internal, you can not use the /reconnect command."));
									return 0;
								}

								// Send feedback to player
								context.getSource().sendFeedback(Text.of("RM-R: /reconnect command called"));

								// Parse current server address
								ServerAddress serverAddress = ServerAddress.parse(currentServer.address);

								// Disconnect user
                                if (client.world != null) {
                                    client.disconnect(client.currentScreen, false);
                                }
//                                client.disconnect();

								// Initiate reconnect sequence
								client.execute(() -> {
									ConnectScreen.connect(null, client, serverAddress, currentServer, true, null);
								});

								return 1;
							})
			);
		});
	}
}
