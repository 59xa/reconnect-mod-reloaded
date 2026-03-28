package io.xa59.reconnect.mixin;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import io.xa59.reconnect.utils.RealmsStateManager;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RealmsMainScreen.class)
public class RealmsMainScreenMixin {
    @Inject(method = "play(Lcom/mojang/realmsclient/dto/RealmsServer;Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"), remap = true)
    private static void captureRealmData(RealmsServer server, Screen lastScreen, CallbackInfo ci) {
        RealmsStateManager.currentRealm = server;
    }
}