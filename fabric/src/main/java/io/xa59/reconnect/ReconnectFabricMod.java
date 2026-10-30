package io.xa59.reconnect;

import io.xa59.reconnect.utils.JoinStatusDisplay;
import io.xa59.reconnect.utils.StatusDisplay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ReconnectFabricMod implements ClientModInitializer {
    public static final String MOD_ID = "reconnect";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final JoinStatusDisplay DISPLAY = new JoinStatusDisplay();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Reconnect: successfully initialised on Fabric.");
        StatusDisplay.setImplementation(DISPLAY);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ReconnectCommands.create(FabricClientCommandSource::sendError)));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ReconnectHandler.handlePostJoin(client);
            DISPLAY.onJoin(client);
        });
    }
}
