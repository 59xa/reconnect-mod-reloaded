package com.xand.reconnectmod.mixin;

import com.xand.reconnectmod.widget.ReconnectButtonWidget;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsServerList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.xand.reconnectmod.ReconnectModReloaded.*;

@Mixin(GameMenuScreen.class)
public abstract class RMixin extends Screen {

	public final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	protected RMixin(Text title) {
		super(title);
	}

	@Inject(at = @At("RETURN"), method = "initWidgets")
	private void addReconnectButton(CallbackInfo ci) {

		// Initialise boolean to determine if the client is in a Singleplayer world
		boolean inSingleplayer = this.client.isInSingleplayer();
		boolean inRealms = false;

		// Determine if the player is in a Realms world
		ServerInfo currentServer = this.client.getCurrentServerEntry();
		if (currentServer != null && currentServer.address != null && currentServer.address.endsWith(".realms.minecraft.net")) {
			inRealms = true;
		}

		// Execute if-statement if the user is not in a Singleplayer world or Realms
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
						ServerAddress serverIp = ServerAddress.parse(currentServer.address);

						// Disconnects player from the server they currently are in
						button.active = false;
						this.client.world.disconnect();
						this.client.disconnect();

						LOGGER.info(ANSI_GREEN + "Successfully disconnected player from world, " +
								ANSI_YELLOW + "now attempting to reconnect user to server");

						// Re-connect player to the multiplayer world they previously joined
						ConnectScreen.connect(null, this.client, serverIp, currentServer, false, null);

						LOGGER.info(ANSI_GREEN + "Successfully reconnected player to current server!");
					},
					(narrationSupplier) -> Text.translatable("reconnectmod.narration.reconnect_button")
			));
		}

		// If the player is in a Realms world, execute this statement instead
		else if (inRealms) {
			LOGGER.info(ANSI_YELLOW + "Player is in a Realms world, displaying the reconnect button");
			MutableText text = (MutableText) Text.of("R");
			this.addDrawableChild(new ReconnectButtonWidget(
					this.width / 2 - 102 + 208,
					this.height / 4 + 120 + -16,
					20,
					20,
					text,
					(button) -> {
						LOGGER.info(ANSI_YELLOW + "Reconnect button pressed, attempting to reconnect player to current Realms server");

						// Disconnects player from the server they currently are in
						button.active = false;
						this.client.world.disconnect();
						this.client.disconnect();

						LOGGER.info(ANSI_GREEN + "Successfully disconnected player from Realms world, " +
								ANSI_YELLOW + "now attempting to reconnect user to Realms server");

						// Use RealmsClient to fetch the current Realms server information and reconnect
						RealmsClient realmsClient = RealmsClient.create();
						try {
							RealmsServerList serverList = realmsClient.listWorlds();
							for (RealmsServer server : serverList.servers) {
								if (server.ownerUUID.equals(this.client.getSession().getUuidOrNull().toString())) {
									this.client.setScreen(new RealmsMainScreen(this));
									realmsClient.join(server.id);
									LOGGER.info(ANSI_GREEN + "Successfully reconnected player to Realms server!");
									break;
								}
							}
						} catch (Exception e) {
							LOGGER.error(ANSI_RED + "Failed to reconnect to Realms server: " + e.getMessage());
						}
					},
					(narrationSupplier) -> Text.translatable("reconnectmod.narration.reconnect_button")
			));
		}
	}
}
