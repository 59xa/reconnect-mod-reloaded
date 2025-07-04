package com.meganpark.reconnectmod.mixin;

import com.meganpark.reconnectmod.widget.ReconnectButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.meganpark.reconnectmod.ReconnectModReloaded.*;

@Mixin(GameMenuScreen.class)
public abstract class RMixin extends Screen {

	public final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	protected RMixin(Text title) {
		super(title);
	}

	@Inject(at = @At("RETURN"), method = "initWidgets")
	private void addReconnectButton(CallbackInfo ci) {

		// Initialise boolean to determine if the client is in a singleplayer world
        assert this.client != null;
        boolean inSingleplayer = this.client.isInSingleplayer();
		boolean inRealms = false;

		// Determine if the player is in a Realms world
		ServerInfo currentServer = this.client.getCurrentServerEntry();
		if (currentServer != null && currentServer.address != null && currentServer.isRealm()) {
			inRealms = true;
		}

		// If the player is neither in singleplayer nor in Realms, add the reconnect button
		if (!inSingleplayer && !inRealms) {
			LOGGER.info(ANSI_YELLOW + "Player not in a singleplayer world or Realms, displaying the reconnect button");
			MutableText text = (MutableText) Text.of("R");
			this.addDrawableChild(new ReconnectButtonWidget(
					this.width / 2 - 102 + 208,
					this.height / 4 + 120 + -16,
					20,
					20,
					text,
					(button) -> {
						LOGGER.info(ANSI_YELLOW + "Reconnect button pressed, attempting to reconnect player to current server");

						// Fetch current server the player is in
                        assert currentServer != null;
                        ServerAddress serverIp = ServerAddress.parse(currentServer.address);

						// Disconnects player from the server they currently are in
						button.active = false;
                        assert this.client.world != null;
                        this.client.world.disconnect(Text.of("RM-R: User requested to reconnect through pause menu"));
						this.client.disconnect(MinecraftClient.getInstance().currentScreen, false);

						LOGGER.info(ANSI_GREEN + "Successfully disconnected player from world, " +
								ANSI_YELLOW + "now attempting to reconnect user to server");

						// Re-connect player to the multiplayer world they previously joined
						// Note: Transfer Packets will prevent the player from re-joining, avoid using CookieStorage for that purpose by making its value "null"
						ConnectScreen.connect(null, this.client, serverIp, currentServer, true, null);

						LOGGER.info(ANSI_GREEN + "Successfully reconnected player to current server!");
					},
					(narrationSupplier) -> Text.translatable("reconnectmod.narration.reconnect_button")
			));
		} else if (inRealms) {
			LOGGER.info(ANSI_YELLOW + "Player is in a Realms world, hiding the reconnect button.");
		}
	}
}
