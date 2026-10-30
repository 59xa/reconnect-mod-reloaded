package io.xa59.reconnect;

import com.mojang.logging.LogUtils;
import io.xa59.reconnect.utils.JoinStatusDisplay;
import io.xa59.reconnect.utils.StatusDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod("reconnect")
public final class ReconnectForgeMod {
    public ReconnectForgeMod() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            Client.initialise();
        }
    }

    private static final class Client {
        private static final JoinStatusDisplay DISPLAY = new JoinStatusDisplay();

        private static void initialise() {
            LogUtils.getLogger().info("Reconnect: successfully initialised on Forge.");
            StatusDisplay.setImplementation(DISPLAY);
            RegisterClientCommandsEvent.BUS.addListener(Client::registerCommands);
            ClientPlayerNetworkEvent.LoggingIn.BUS.addListener(Client::onJoin);
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
