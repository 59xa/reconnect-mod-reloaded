package com.meganpark.reconnectmod.widget;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;

public class ReconnectButtonWidget extends ButtonWidget {

    // Defining Constructor with 6 specific arguments to match with ButtonWidget
    public ReconnectButtonWidget(int x, int y, int width, int height, MutableText message, PressAction onPress, NarrationSupplier narrationSupplier) {
        super(x, y, width, height, message, onPress, narrationSupplier);
    }
}
