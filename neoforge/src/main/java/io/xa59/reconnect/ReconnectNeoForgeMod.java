package io.xa59.reconnect;

import net.neoforged.bus.api.SubscribeEvent;
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
    }

    public ReconnectNeoForgeMod() {
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void registerCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("reconnect")
                        .executes(ctx -> ReconnectHandler.reconnect(Minecraft.getInstance()))
        );
    }
}
