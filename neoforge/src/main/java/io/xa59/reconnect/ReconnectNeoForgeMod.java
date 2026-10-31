package io.xa59.reconnect;

import com.mojang.logging.LogUtils;
import io.xa59.reconnect.utils.JoinStatusDisplay;
import io.xa59.reconnect.utils.StatusDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.fml.common.Mod;
import io.xa59.reconnect.utils.NeoForgeEnvironment;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod("reconnect")
public final class ReconnectNeoForgeMod {
    public ReconnectNeoForgeMod() {
        if (NeoForgeEnvironment.isClient()) {
            Client.initialise();
        }
    }

    private static final class Client {
        private static final JoinStatusDisplay DISPLAY = new JoinStatusDisplay();

        private static void initialise() {
            LogUtils.getLogger().info("Reconnect: successfully initialised on NeoForge.");
            StatusDisplay.setImplementation(DISPLAY);
            NeoForge.EVENT_BUS.addListener(Client::registerCommands);
            NeoForge.EVENT_BUS.addListener(Client::onJoin);
        }

        private static void registerCommands(RegisterClientCommandsEvent event) {
            event.getDispatcher().register(ReconnectCommands.create(CommandSourceStack::sendFailure));
        }

        private static void onJoin(ClientPlayerNetworkEvent.LoggingIn event) {
            Minecraft client = Minecraft.getInstance();
            client.execute(() -> {
                ReconnectHandler.handlePostJoin(client);
                DISPLAY.onJoin(client);
            });
        }
    }
}
