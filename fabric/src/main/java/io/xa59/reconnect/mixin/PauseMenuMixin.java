package io.xa59.reconnect.mixin;

import static io.xa59.reconnect.ReconnectFabricMod.*;

import io.xa59.reconnect.utils.StatusDisplay;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseMenuMixin extends Screen {

	@Unique
    public final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	protected PauseMenuMixin(Component title) {
		super(title);
	}
	
	@Unique
    private boolean reconnectTriggered = false;

	@Inject(at = @At("RETURN"), method = "createPauseMenu")
	private void addReconnectButton(CallbackInfo ci) {

		// Initialise boolean to determine if the client is in a singleplayer world
        boolean inSingleplayer = this.minecraft.isLocalServer();
		boolean inRealms = false;

		// Determine if the player is in a Realms world
		ServerData currentServer = this.minecraft.getCurrentServer();
		if (currentServer != null && currentServer.isRealm()) {
			inRealms = true;
		}

		// If the player is neither in singleplayer nor in Realms, add the reconnect button
		if (!inSingleplayer && !inRealms) {
			LOGGER.info(ANSI_YELLOW + "Player not in a singleplayer world or Realms, initialising reconnect button");
			this.addRenderableWidget(Button.builder(
				Component.literal("R"),
				button -> 
				{
					LOGGER.info(ANSI_YELLOW + "Reconnect button pressed, attempting to reconnect player to current server");

						// Fetch current server the player is in
                        assert currentServer != null;
                        ServerAddress serverIp = ServerAddress.parseString(currentServer.ip);

						// Disconnects player from the server they currently are in
						button.active = false;
                        assert this.minecraft.level != null;
                        this.minecraft.level.disconnect(Component.nullToEmpty("[Reconnect]: User requested to reconnect through pause menu."));

						assert Minecraft.getInstance().screen != null;
						this.minecraft.disconnect(Minecraft.getInstance().screen, false);
						
						reconnectTriggered = true;
						
						LOGGER.info(ANSI_GREEN + "Successfully disconnected player from world, " +
								ANSI_YELLOW + "now attempting to reconnect user to server.");

						// Re-connect player to the multiplayer world they previously joined
						// Note: Transfer Packets will prevent the player from re-joining, avoid using CookieStorage for that purpose by making its value "null"
						ConnectScreen.startConnecting(null, this.minecraft, serverIp, currentServer, true, null);

						ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
							if (!reconnectTriggered) return;

							reconnectTriggered = false;

							if (client.player != null) {
								client.player.sendOverlayMessage(
									Component.literal("Successfully reconnected.").withStyle(ChatFormatting.GREEN)
								);

								StatusDisplay.resetOverlay();
								StatusDisplay.sendOverlayMessageAfterJoin(
									"Successfully reconnected.",
									ChatFormatting.GREEN
								);
							}
						});

						ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
							reconnectTriggered = false;
						});
						
						LOGGER.info(ANSI_GREEN + "Successfully reconnected player to current server.");
				})
				.bounds(this.width / 2 - 102 + 208, this.height / 4 + 120 + -16, 20, 20)
				.build()
			);
		} else if (inRealms) {
			LOGGER.info(ANSI_YELLOW + "Player is in a Realms world, hiding the reconnect button.");
		}
	}
}
