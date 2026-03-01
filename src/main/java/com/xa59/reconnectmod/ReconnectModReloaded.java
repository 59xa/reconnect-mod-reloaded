package com.xa59.reconnectmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xa59.reconnectmod.utils.StatusDisplay;

public class ReconnectModReloaded implements ClientModInitializer {
	public static final String MOD_ID = "reconnect-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";

	private boolean reconnectTriggered = false;

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
							client.player.sendOverlayMessage(Component.literal("You are currently not connected to a multiplayer server.").withStyle(ChatFormatting.RED));
						}
						return 0;
					}

					if (currentServer.isRealm()) {
						client.player.sendOverlayMessage(Component.literal("Due to the Realms API being internal, you can not use the /reconnect command.").withStyle(ChatFormatting.RED));
						return 0;
					}

					reconnectTriggered = true;

					// Parse current server address
					ServerAddress serverAddress = ServerAddress.parseString(currentServer.ip);

					// Disconnect user
					if (client.level != null) {
						client.level.disconnect(Component.literal("RM-R: User requested reconnect sequence using /reconnect."));
					}
					client.disconnect(client.screen, false);

					// Initiate reconnect sequence
					client.execute(() -> {
						ConnectScreen.startConnecting(null, client, serverAddress, currentServer, true, null);
					});
					
					if (reconnectTriggered) {
						StatusDisplay.resetOverlay();
						StatusDisplay.sendOverlayMessageAfterJoin("Successfully reconnected.", ChatFormatting.GREEN);
					}
					
					reconnectTriggered = false;
					return 1;
				})
			);
		});
	}
}
