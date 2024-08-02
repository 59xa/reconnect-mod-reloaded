package com.xand.reconnectmod.widget;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.client.gui.widget.ButtonWidget.NarrationSupplier;

public class ReconnectButtonWidget extends ButtonWidget {

    // Constructor with 6 arguments to match the base ButtonWidget constructor
    public ReconnectButtonWidget(int x, int y, int width, int height, MutableText message, PressAction onPress, NarrationSupplier narrationSupplier) {
        super(x, y, width, height, message, onPress, narrationSupplier);
    }
}
