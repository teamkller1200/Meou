package com.aibots.client.screen;

import com.aibots.Aibots;
import com.aibots.entity.skill.AiluuSkill;
import com.aibots.screen.AiluuScreenHandler;
import com.aibots.screen.SkillSelectPayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class AiluuScreen extends AbstractContainerScreen<AiluuScreenHandler> {
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(Aibots.MOD_ID, "textures/gui/container/ailuu.png");

    private static final ResourceLocation BUTTON = ResourceLocation.withDefaultNamespace("widget/button");
    private static final ResourceLocation BUTTON_HIGHLIGHTED =
        ResourceLocation.withDefaultNamespace("widget/button_highlighted");
    private static final ResourceLocation BUTTON_DISABLED =
        ResourceLocation.withDefaultNamespace("widget/button_disabled");

    private static final int PANEL_COLOR = 0xFFC6C6C6;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private static final int TAB_INVENTORY = 0;
    private static final int TAB_SKILL = 1;
    private static final int TAB_WIDTH = 40;
    private static final int TAB_HEIGHT = 12;

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

        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        this.renderTabs(graphics, x, y);

        if (this.currentTab == TAB_SKILL) {
            this.renderSkillTab(graphics, x, y);
        }
    }

    private void renderTabs(GuiGraphics graphics, int x, int y) {
        this.renderTab(graphics, x + 88, y + 2, TAB_INVENTORY,
            Component.translatable("tab.aibots.inventory"));
        this.renderTab(graphics, x + 130, y + 2, TAB_SKILL,
            Component.translatable("tab.aibots.skill"));
    }

    private void renderTab(GuiGraphics graphics, int x, int y, int tab, Component label) {
        boolean active = this.currentTab == tab;
        graphics.blitSprite(active ? BUTTON_HIGHLIGHTED : BUTTON, x, y, TAB_WIDTH, TAB_HEIGHT);
        this.drawCenteredShadow(graphics, label, x + TAB_WIDTH / 2, y + 2, TEXT_COLOR);
    }

    private void renderSkillTab(GuiGraphics graphics, int x, int y) {
        graphics.fill(x + 7, y + 39, x + 7 + 162, y + 39 + 120, PANEL_COLOR);
        for (AiluuSkill skill : AiluuSkill.values()) {
            int idx = skill.ordinal();
            int col = idx % 2;
            int row = idx / 2;
            int bx = x + 7 + col * 79;
            int by = y + 46 + row * 22;
            boolean selected = idx == this.menu.getSelectedSkillIndex();
            graphics.blitSprite(selected ? BUTTON_DISABLED : BUTTON, bx, by, 72, 18);
            this.drawCenteredShadow(graphics, skillName(skill), bx + 36, by + 5, TEXT_COLOR);
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
            if (this.inRect(x + 88, y + 2, TAB_WIDTH, TAB_HEIGHT, mouseX, mouseY)) {
                this.setTab(TAB_INVENTORY);
                return true;
            }
            if (this.inRect(x + 130, y + 2, TAB_WIDTH, TAB_HEIGHT, mouseX, mouseY)) {
                this.setTab(TAB_SKILL);
                return true;
            }
            if (this.currentTab == TAB_SKILL) {
                for (AiluuSkill skill : AiluuSkill.values()) {
                    int idx = skill.ordinal();
                    int col = idx % 2;
                    int row = idx / 2;
                    int bx = x + 7 + col * 79;
                    int by = y + 46 + row * 22;
                    if (this.inRect(bx, by, 72, 18, mouseX, mouseY)) {
                        if (idx != this.menu.getSelectedSkillIndex()) {
                            this.selectSkill(skill);
                        }
                        return true;
                    }
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void setTab(int tab) {
        this.currentTab = tab;
        this.menu.setSkillTab(tab == TAB_SKILL);
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

    private void drawCenteredShadow(GuiGraphics graphics, Component text, int centerX, int y, int color) {
        graphics.drawString(this.font, text, centerX - this.font.width(text) / 2, y, color, true);
    }
}
