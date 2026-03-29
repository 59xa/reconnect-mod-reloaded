package io.xa59.reconnect;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.xa59.reconnect.utils.FabricStatusDisplay;
import io.xa59.reconnect.utils.StatusDisplay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.xa59.reconnect.utils.ArgumentUtils;

public class ReconnectFabricMod implements ClientModInitializer {
	public static final String MOD_ID = "reconnect";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";

	@Override
	public void onInitializeClient() {
		LOGGER.info(ANSI_GREEN + "Reconnect" + ANSI_YELLOW + ": successfully initialised on Fabric." + ANSI_RESET);

		StatusDisplay.setImplementation(new FabricStatusDisplay());

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> {
			dispatcher.register(
					ClientCommands.literal("reconnect")
							.then(ClientCommands.literal("execute")

									// "/reconnect execute <command>"
									.then(ClientCommands.argument("command", StringArgumentType.greedyString())
											.executes(ctx -> {
												String cmd = StringArgumentType.getString(ctx, "command");
												cmd = ArgumentUtils.cleanupCommandInput(cmd);

												if (cmd == null || cmd.isEmpty()) {
													ctx.getSource().sendError(Component.literal("[Reconnect] Command cannot be empty."));
													return 0;
												}

												ArgumentUtils.setPostCommand(cmd);

												// Ensure delay is reset when not provided
												ArgumentUtils.setDelay("0s");

												return ReconnectHandler.reconnect(Minecraft.getInstance());
											})
									)

									// "/reconnect execute delay <time> <command>"
									.then(ClientCommands.literal("delay")
											.then(ClientCommands.argument("time", StringArgumentType.word())
													.then(ClientCommands.argument("command", StringArgumentType.greedyString())
															.executes(ctx -> {
																String cmd = StringArgumentType.getString(ctx, "command");
																cmd = ArgumentUtils.cleanupCommandInput(cmd);

																String time = StringArgumentType.getString(ctx, "time");

																if (cmd == null || cmd.isEmpty()) {
																	ctx.getSource().sendError(Component.literal("[Reconnect] Command cannot be empty."));
																	return 0;
																}

																ArgumentUtils.setPostCommand(cmd);
																ArgumentUtils.setDelay(time);

																return ReconnectHandler.reconnect(Minecraft.getInstance());
															})
													)
											)
									)
							)
			);
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			ReconnectHandler.handlePostJoin(client);
		});
	}
}
