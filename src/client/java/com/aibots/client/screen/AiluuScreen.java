package com.aibots.client.screen;

import com.aibots.entity.skill.AiluuSkill;
import com.aibots.screen.AiluuScreenHandler;
import com.aibots.screen.SkillSelectPayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class AiluuScreen extends AbstractContainerScreen<AiluuScreenHandler> {
    private static final int BG_COLOR = 0xC0101010;
    private static final int SLOT_COLOR = 0x55FFFFFF;
    private static final int BORDER_COLOR = 0x30808080;
    private static final int TAB_COLOR = 0xC0262626;
    private static final int TAB_ACTIVE_COLOR = 0xC03E5C3E;

    private static final int TAB_INVENTORY = 0;
    private static final int TAB_SKILL = 1;

    private int currentTab = TAB_INVENTORY;

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

        this.renderTabs(graphics, x, y);

        if (this.currentTab == TAB_SKILL) {
            this.renderSkillTab(graphics, x, y);
        } else {
            this.renderSlotBoxes(graphics, x, y);
        }
    }

    private void renderTabs(GuiGraphics graphics, int x, int y) {
        this.renderTab(graphics, x + 100, y + 5, 30, 11, TAB_INVENTORY,
            Component.translatable("tab.aibots.inventory"));
        this.renderTab(graphics, x + 132, y + 5, 30, 11, TAB_SKILL,
            Component.translatable("tab.aibots.skill"));
    }

    private void renderTab(GuiGraphics graphics, int x, int y, int w, int h, int tab, Component label) {
        int color = this.currentTab == tab ? TAB_ACTIVE_COLOR : TAB_COLOR;
        graphics.fill(x, y, x + w, y + h, color);
        graphics.fill(x, y, x + w, y + 1, BORDER_COLOR);
        graphics.fill(x, y, x + 1, y + h, BORDER_COLOR);
        graphics.fill(x + w - 1, y, x + w, y + h, BORDER_COLOR);
        graphics.drawString(this.font, label, x + 3, y + 1, 0xFFFFFFFF, false);
    }

    private void renderSlotBoxes(GuiGraphics graphics, int x, int y) {
        for (var slot : this.menu.slots) {
            int sx = x + slot.x;
            int sy = y + slot.y;
            graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, SLOT_COLOR);
            graphics.fill(sx, sy, sx + 18, sy + 18, BORDER_COLOR);
        }
    }

    private void renderSkillTab(GuiGraphics graphics, int x, int y) {
        for (AiluuSkill skill : AiluuSkill.values()) {
            int idx = skill.ordinal();
            int col = idx % 2;
            int row = idx / 2;
            int bx = x + 7 + col * 79;
            int by = y + 39 + row * 18;
            boolean selected = idx == this.menu.getSelectedSkillIndex();
            int fillColor = selected ? TAB_ACTIVE_COLOR : TAB_COLOR;
            graphics.fill(bx, by, bx + 72, by + 16, fillColor);
            graphics.fill(bx, by, bx + 72, by + 1, BORDER_COLOR);
            graphics.fill(bx, by, bx + 1, by + 16, BORDER_COLOR);
            graphics.fill(bx + 71, by, bx + 72, by + 16, BORDER_COLOR);
            graphics.fill(bx, by + 15, bx + 72, by + 16, BORDER_COLOR);
            graphics.drawString(this.font, skillName(skill), bx + 4, by + 4, 0xFFFFFFFF, false);
        }
    }

    @Override
    protected void renderSlot(GuiGraphics graphics, Slot slot) {
        if (this.currentTab == TAB_SKILL) {
            return;
        }
        super.renderSlot(graphics, slot);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.currentTab == TAB_SKILL) {
            return;
        }
        super.renderLabels(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = this.leftPos;
            int y = this.topPos;
            if (this.inRect(x + 100, y + 5, 30, 11, mouseX, mouseY)) {
                this.currentTab = TAB_INVENTORY;
                return true;
            }
            if (this.inRect(x + 132, y + 5, 30, 11, mouseX, mouseY)) {
                this.currentTab = TAB_SKILL;
                return true;
            }
            if (this.currentTab == TAB_SKILL) {
                for (AiluuSkill skill : AiluuSkill.values()) {
                    int idx = skill.ordinal();
                    int col = idx % 2;
                    int row = idx / 2;
                    int bx = x + 7 + col * 79;
                    int by = y + 39 + row * 18;
                    if (this.inRect(bx, by, 72, 16, mouseX, mouseY)) {
                        this.selectSkill(skill);
                        return true;
                    }
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void selectSkill(AiluuSkill skill) {
        ClientPlayNetworking.send(new SkillSelectPayload(this.menu.getEntityId(), skill.ordinal()));
    }

    private static Component skillName(AiluuSkill skill) {
        return Component.translatable("skill.aibots." + skill.getKey());
    }

    private boolean inRect(int bx, int by, int w, int h, double mouseX, double mouseY) {
        return mouseX >= bx && mouseX < bx + w && mouseY >= by && mouseY < by + h;
    }
}
