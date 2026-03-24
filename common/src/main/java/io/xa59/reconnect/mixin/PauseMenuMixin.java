package io.xa59.reconnect.mixin;

import io.xa59.reconnect.ReconnectHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseMenuMixin extends Screen {
    protected PauseMenuMixin(Component title) {
        super(title);
    }

    @Inject(method = "createPauseMenu", at = @At("RETURN"))
    private void reconnect$addButton(CallbackInfo ci) {
        Minecraft minecraft = this.minecraft;

        // Initialise boolean to determine if the client is in a singleplayer world
        boolean inSingleplayer = this.minecraft.isLocalServer();

        ServerData currentServer = minecraft.getCurrentServer();
        boolean inRealms = currentServer != null && currentServer.isRealm();

        // Only show in multiplayer (not in Singleplayer or Realms)
        if (inSingleplayer || inRealms || currentServer == null) return;

        Button disconnectButton = null;
        for (var widget : this.children()) {
            // Check if component contains translatable content, then check key
            if (widget instanceof Button b && b.getMessage().getContents() instanceof TranslatableContents tc) {
                if ("menu.disconnect".equals(tc.getKey())) {
                    disconnectButton = b;
                    break;
                }
            }
        }

        int bW = 20;
        int bH = 20;
        int gap = 4;

        int x = (disconnectButton != null) ? disconnectButton.getX() + disconnectButton.getWidth() + gap : this.width / 2 + 102;
        int y = (disconnectButton != null) ? disconnectButton.getY() : this.height / 4 + 120 - 16;

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("R"),
                                _ -> ReconnectHandler.reconnect(minecraft)
                )
                .bounds(x, y, bW, bH)
                .build()
        );
    }
}
