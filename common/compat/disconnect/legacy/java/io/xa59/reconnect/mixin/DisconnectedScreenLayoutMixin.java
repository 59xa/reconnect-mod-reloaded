package io.xa59.reconnect.mixin;

import io.xa59.reconnect.utils.DisconnectedScreenLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenLayoutMixin implements DisconnectedScreenLayout {
    @Shadow @Final private GridLayout layout;

    @Override
    public void reconnect$addControls(GridLayout controls) {
        layout.addChild(controls, 3, 0, layout.newCellSettings().alignHorizontallyCenter());
        layout.arrangeElements();
    }
}
