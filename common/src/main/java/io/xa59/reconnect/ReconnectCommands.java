package io.xa59.reconnect;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.xa59.reconnect.utils.ArgumentUtils;
import net.minecraft.network.chat.Component;

import java.util.function.BiConsumer;

public final class ReconnectCommands {
    public static <S> LiteralArgumentBuilder<S> create(BiConsumer<S, Component> sendError) {
        return LiteralArgumentBuilder.<S>literal("reconnect")
                .executes(context -> ReconnectHandler.reconnect())
                .then(LiteralArgumentBuilder.<S>literal("execute")
                        .then(RequiredArgumentBuilder.<S, String>argument("command", StringArgumentType.greedyString())
                                .executes(context -> execute(context, sendError, "0s")))
                        .then(LiteralArgumentBuilder.<S>literal("delay")
                                .then(RequiredArgumentBuilder.<S, String>argument("time", StringArgumentType.word())
                                        .then(RequiredArgumentBuilder.<S, String>argument("command", StringArgumentType.greedyString())
                                                .executes(context -> execute(context, sendError,
                                                        StringArgumentType.getString(context, "time")))))));
    }

    private static <S> int execute(CommandContext<S> context, BiConsumer<S, Component> sendError, String delay) {
        String command = ArgumentUtils.cleanupCommandInput(StringArgumentType.getString(context, "command"));
        if (command.isEmpty()) {
            sendError.accept(context.getSource(), Component.literal("[Reconnect] Command cannot be empty."));
            return 0;
        }
        if (command.split("\\s+", 2)[0].equalsIgnoreCase("reconnect")) {
            sendError.accept(context.getSource(), Component.literal("<59xa> bro, don't even try recursing /reconnect lmfao"));
            return 0;
        }
        ArgumentUtils.setPostCommand(command);
        ArgumentUtils.setDelay(delay);
        return ReconnectHandler.reconnect();
    }
}
