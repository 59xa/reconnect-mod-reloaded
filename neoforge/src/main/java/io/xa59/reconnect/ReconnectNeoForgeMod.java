package io.xa59.reconnect;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "reconnect", dist = Dist.CLIENT)
public class ReconnectNeoForgeMod {
    public ReconnectNeoForgeMod() {
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void registerCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("reconnect")
                        .executes(ctx -> ReconnectCommands.reconnect(Minecraft.getInstance()))
        );
    }
}
