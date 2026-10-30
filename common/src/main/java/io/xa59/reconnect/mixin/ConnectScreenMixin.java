package io.xa59.reconnect.mixin;

import io.xa59.reconnect.AutoReconnectHandler;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin extends Screen {
    @Shadow private volatile boolean aborted;

    protected ConnectScreenMixin(Component title) {
        super(title);
    }

    @ModifyVariable(method = "startConnecting", at = @At("HEAD"), argsOnly = true)
    private static ServerData reconnect$rememberServer(ServerData server) {
        AutoReconnectHandler.rememberServer(server);
        return server;
    }

    @Override
    public void removed() {
        super.removed();
        if (aborted) {
            AutoReconnectHandler.cancel();
        }
    }
}
