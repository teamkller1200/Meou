package com.aibots.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import com.aibots.screen.AiluuScreenHandler;

public class AiluuScreen extends AbstractContainerScreen<AiluuScreenHandler> {
    private static final int BG_COLOR = 0xC0101010;
    private static final int SLOT_COLOR = 0x55FFFFFF;
    private static final int BORDER_COLOR = 0x30808080;

    public AiluuScreen(AiluuScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 182;
        this.titleLabelX = 7;
        this.titleLabelY = 5;
        this.inventoryLabelX = 7;
        this.inventoryLabelY = 95;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, BG_COLOR);

        for (var slot : this.menu.slots) {
            int sx = x + slot.x;
            int sy = y + slot.y;
            graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, SLOT_COLOR);
            graphics.fill(sx, sy, sx + 18, sy + 18, BORDER_COLOR);
        }
    }
}