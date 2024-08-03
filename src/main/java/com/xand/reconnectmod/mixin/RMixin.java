package com.xand.reconnectmod.mixin;

import com.xand.reconnectmod.widget.ReconnectButtonWidget;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.session.Session;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.xand.reconnectmod.ReconnectModReloaded.*;

@Mixin(GameMenuScreen.class)
public abstract class RMixin extends Screen {

	public final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	protected RMixin(Text title) {
		super(title);
	}

	@Inject(at = @At("RETURN"), method = "initWidgets")
	private void addReconnectButton(CallbackInfo ci) {

		// Initialise boolean to determine if the client is in a singleplayer world
		boolean inSingleplayer = this.client.isInSingleplayer();

		// Execute if-statement if the user is not in a singleplayer world
		if (!inSingleplayer) {
			LOGGER.info(ANSI_YELLOW + "Player not in a singleplayer world, displaying the reconnect button");
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
						ServerInfo currentServer = this.client.getCurrentServerEntry();
						ServerAddress serverIp = ServerAddress.parse(currentServer.address);

						// Disconnects player from the server they currently are in
						button.active = false;
						this.client.world.disconnect();
						this.client.disconnect();

						LOGGER.info(ANSI_GREEN + "Successfully disconnected player from world, " +
								ANSI_YELLOW + "now attempting to fetch player and client session data");

						// Fetch current session of the client including player identification
						Session session = this.client.getSession();
						String playerUsername = session.getUsername();
						UUID playerUUID = session.getUuidOrNull();
						String playerAccessToken = session.getAccessToken();
						Optional<String> clientXuid = session.getXuid();
						Optional<String> clientID = session.getClientId();
						Session.AccountType playerType = session.getAccountType();

						LOGGER.info(ANSI_GREEN + "Successfully retrieved player and client session data");

						// Put session information into a Map interface for CookieStorage to use when re-initialising session
						Map<Identifier, byte[]> sessionMap = new HashMap<>();
						sessionMap.put(Identifier.of("reconnectmod:username"), playerUsername.getBytes(StandardCharsets.UTF_8));
						if (playerUUID != null) {
							sessionMap.put(Identifier.of("reconnectmod:uuid"), playerUUID.toString().getBytes(StandardCharsets.UTF_8));
						}
						sessionMap.put(Identifier.of("reconnectmod:access_token"), playerAccessToken.getBytes(StandardCharsets.UTF_8));
						clientXuid.ifPresent(xuid -> sessionMap.put(Identifier.of("reconnectmod:xuid"), xuid.getBytes(StandardCharsets.UTF_8)));
						clientID.ifPresent(id -> sessionMap.put(Identifier.of("reconnectmod:client_id"), id.getBytes(StandardCharsets.UTF_8)));
						sessionMap.put(Identifier.of("reconnectmod:account_type"), playerType.getName().getBytes(StandardCharsets.UTF_8));

						LOGGER.info(ANSI_GREEN + "Current user and client data successfully added to Map interface, " + ANSI_YELLOW + "now attempting to reconnect user to server");
						// Initialise CookieStorage with provided Map interface information
						CookieStorage cookieStorage = new CookieStorage(sessionMap);

						// Re-connect player to the multiplayer world they previously joined
						ConnectScreen.connect(null, this.client, serverIp, currentServer, true, cookieStorage);

						LOGGER.info(ANSI_GREEN + "Successfully reconnected player to current server!");
					},
					(narrationSupplier) -> Text.translatable("reconnectmod.narration.reconnect_button")
			));
		}
	}
}
