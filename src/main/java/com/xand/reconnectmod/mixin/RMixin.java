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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Mixin(GameMenuScreen.class)
public abstract class RMixin extends Screen {

	protected RMixin(Text title) {
		super(title);
	}

	@Inject(at = @At("RETURN"), method = "initWidgets")
	private void addReconnectButton(CallbackInfo ci) {

		boolean inSingleplayer = this.client.isInSingleplayer();

		if (!inSingleplayer) {
			MutableText text = (MutableText) Text.of("R");
			this.addDrawableChild(new ReconnectButtonWidget(
					this.width / 2 - 102 + 208,
					this.height / 4 + 120 + -16,
					20,
					20,
					text,
					(button) -> {
						ServerInfo currentServer = this.client.getCurrentServerEntry();
						ServerAddress serverIp = ServerAddress.parse(currentServer.address);
						System.out.println(serverIp);

						button.active = false;
						this.client.world.disconnect();
						this.client.disconnect();

						Session session = this.client.getSession();
						String playerUsername = session.getUsername();
						UUID playerUUID = session.getUuidOrNull();
						String playerAccessToken = session.getAccessToken();
						Optional<String> clientXuid = session.getXuid();
						Optional<String> clientID = session.getClientId();
						Session.AccountType playerType = session.getAccountType();

						Map<Identifier, byte[]> sessionMap = new HashMap<>();
						sessionMap.put(Identifier.of("reconnectmod:username"), playerUsername.getBytes(StandardCharsets.UTF_8));
						if (playerUUID != null) {
							sessionMap.put(Identifier.of("reconnectmod:uuid"), playerUUID.toString().getBytes(StandardCharsets.UTF_8));
						}
						sessionMap.put(Identifier.of("reconnectmod:access_token"), playerAccessToken.getBytes(StandardCharsets.UTF_8));
						clientXuid.ifPresent(xuid -> sessionMap.put(Identifier.of("reconnectmod:xuid"), xuid.getBytes(StandardCharsets.UTF_8)));
						clientID.ifPresent(id -> sessionMap.put(Identifier.of("reconnectmod:client_id"), id.getBytes(StandardCharsets.UTF_8)));
						sessionMap.put(Identifier.of("reconnectmod:account_type"), playerType.getName().getBytes(StandardCharsets.UTF_8));

						CookieStorage cookieStorage = new CookieStorage(sessionMap);
						ConnectScreen.connect(null, this.client, serverIp, currentServer, true, cookieStorage);
					},
					(narrationSupplier) -> Text.translatable("reconnectmod.narration.reconnect_button")
			));
		}
	}
}
