package io.xa59.reconnect.mixin;

import io.xa59.reconnect.AutoReconnectHandler;
import io.xa59.reconnect.utils.DisconnectedScreenLayout;
import io.xa59.reconnect.utils.RetrySchedule;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin extends Screen {
    @Shadow @Final private Screen parent;
    @Unique private RetrySchedule reconnect$schedule;
    @Unique private StringWidget reconnect$countdown;
    @Unique private Button reconnect$infinite;

    protected DisconnectedScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void reconnect$initialise(CallbackInfo ci) {
        if (!minecraft.allowsMultiplayer()) {
            return;
        }
        reconnect$schedule = AutoReconnectHandler.disconnected();
        if (reconnect$schedule == null) {
            return;
        }
        GridLayout controls = new GridLayout().rowSpacing(4);
        controls.defaultCellSetting().alignHorizontallyCenter();
        controls.addChild(Button.builder(Component.literal("Reconnect Now"), button -> reconnect$connect(false))
                .width(200).build(), 0, 0);
        GridLayout countdownRow = new GridLayout().columnSpacing(4);
        reconnect$countdown = countdownRow.addChild(new StringWidget(176, 20, Component.empty(), font), 0, 0);
        reconnect$infinite = countdownRow.addChild(Button.builder(Component.literal("∞"), button -> {
            reconnect$schedule.toggleInfinite(System.nanoTime());
            reconnect$updateLabels();
            reconnect$updateInfinite();
        }).width(20).build(), 0, 1);
        controls.addChild(countdownRow, 1, 0);
        ((DisconnectedScreenLayout) this).reconnect$addControls(controls);
        controls.visitWidgets(widget -> addRenderableWidget(widget));
        repositionElements();
        reconnect$updateLabels();
        reconnect$updateInfinite();
    }

    @Unique
    private void reconnect$connect(boolean automatic) {
        AutoReconnectHandler.reconnect(minecraft, parent, reconnect$schedule, automatic);
    }

    @Unique
    private void reconnect$updateLabels() {
        String text = reconnect$schedule.isWaiting()
                ? "Reconnecting in " + reconnect$schedule.remainingSeconds(System.nanoTime()) + " seconds"
                : "Automatic reconnect stopped";
        reconnect$countdown.setMessage(Component.literal(text));
    }

    @Unique
    private void reconnect$updateInfinite() {
        boolean infinite = reconnect$schedule.isInfinite();
        reconnect$infinite.setMessage(Component.literal("∞").withStyle(infinite ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        reconnect$infinite.setTooltip(Tooltip.create(Component.literal(infinite
                ? "Infinite reconnect: on (every 3 seconds)"
                : "Infinite reconnect: off")));
    }

    @Override
    public void tick() {
        super.tick();
        if (reconnect$schedule != null) {
            reconnect$updateLabels();
            if (reconnect$schedule.isDue(System.nanoTime())) {
                reconnect$connect(true);
            }
        }
    }

    @Override
    public void removed() {
        super.removed();
        AutoReconnectHandler.leaveDisconnected(reconnect$schedule);
    }
}
