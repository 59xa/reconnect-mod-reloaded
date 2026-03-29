package io.xa59.reconnect;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.xa59.reconnect.utils.ArgumentUtils;
import io.xa59.reconnect.utils.NeoForgeStatusDisplay;
import io.xa59.reconnect.utils.StatusDisplay;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "reconnect", dist = Dist.CLIENT)
public class ReconnectNeoForgeMod {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    @SubscribeEvent
    public void onClientStarting(ClientStartedEvent event) {
        LOGGER.info(ANSI_GREEN + "Reconnect" + ANSI_YELLOW + ": successfully initialised on NeoForge." + ANSI_RESET);

        StatusDisplay.setImplementation(new NeoForgeStatusDisplay());
    }

    @SubscribeEvent
    public void onJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        Minecraft client = Minecraft.getInstance();

        client.execute(() -> {
            ReconnectHandler.handlePostJoin(client);
        });
    }

    public ReconnectNeoForgeMod() {
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(this::onJoin);
    }

    private void registerCommands(RegisterClientCommandsEvent event) {
        // Register the /reconnect command
        event.getDispatcher().register(
                Commands.literal("reconnect")
                        .executes(_ -> ReconnectHandler.reconnect(Minecraft.getInstance()))

                        .then(Commands.literal("execute")

                                // "/reconnect execute <command>"
                                .then(Commands.argument("command", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String cmd = StringArgumentType.getString(ctx, "command");
                                            cmd = ArgumentUtils.cleanupCommandInput(cmd);

                                            if (cmd == null || cmd.isEmpty()) {
                                                ctx.getSource().sendFailure(Component.literal("[Reconnect] Command cannot be empty."));
                                                return 0;
                                            }

                                            ArgumentUtils.setPostCommand(cmd);

                                            // Ensure delay is reset when not provided
                                            ArgumentUtils.setDelay("0s");

                                            return ReconnectHandler.reconnect(Minecraft.getInstance());
                                        })
                                )

                                // "/reconnect execute delay <time> <command>"
                                .then(Commands.literal("delay")
                                        .then(Commands.argument("time", StringArgumentType.word())
                                                .then(Commands.argument("command", StringArgumentType.greedyString())
                                                        .executes(ctx -> {
                                                            String cmd = StringArgumentType.getString(ctx, "command");
                                                            cmd = ArgumentUtils.cleanupCommandInput(cmd);

                                                            String time = StringArgumentType.getString(ctx, "time");

                                                            if (cmd == null || cmd.isEmpty()) {
                                                                ctx.getSource().sendFailure(Component.literal("[Reconnect] Command cannot be empty."));
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
    }
}
