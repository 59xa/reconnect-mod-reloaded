package io.xa59.reconnect;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReconnectFabricMod implements ClientModInitializer {
	public static final String MOD_ID = "reconnect";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";

	private boolean reconnectTriggered = false;

	@Override
	public void onInitializeClient() {
		LOGGER.info(ANSI_GREEN + "Reconnect" + ANSI_YELLOW + ": successfully initialised on Fabric." + ANSI_RESET);

		// Register the /reconnect command
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
					ClientCommands.literal("reconnect")
							.executes(ctx -> ReconnectHandler.reconnect(Minecraft.getInstance()))
			);
		});
	}
}
