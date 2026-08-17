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
    private static final ResourceLocation TAB_SELECTED =
        ResourceLocation.fromNamespaceAndPath(Aibots.MOD_ID, "textures/gui/ailuu_tab_selected.png");
    private static final ResourceLocation TAB_UNSELECTED =
        ResourceLocation.fromNamespaceAndPath(Aibots.MOD_ID, "textures/gui/ailuu_tab_unselected.png");

    private static final ResourceLocation BUTTON = ResourceLocation.withDefaultNamespace("widget/button");
    private static final ResourceLocation BUTTON_DISABLED =
        ResourceLocation.withDefaultNamespace("widget/button_disabled");

    private static final int PANEL_COLOR = 0xFFC6C6C6;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private static final int TAB_INVENTORY = 0;
    private static final int TAB_SKILL = 1;
    private static final int TAB_WIDTH = 54;
    private static final int TAB_HEIGHT = 32;
    private static final int TAB_X = 7;
    private static final int TAB_Y = -28;

    private static final int INVENTORY_WIDTH = 176;
    private static final int INVENTORY_HEIGHT = 182;
    private static final int SKILL_WIDTH = 176;
    private static final int SKILL_HEIGHT = 164;

    private static final int SKILL_BTN_W = 72;
    private static final int SKILL_BTN_H = 18;
    private static final int SKILL_COL_PITCH = 79;
    private static final int SKILL_ROW_PITCH = 22;
    private static final int SKILL_GRID_X = 12;
    private static final int SKILL_GRID_Y = 46;

    private int currentTab = TAB_INVENTORY;

    public AiluuScreen(AiluuScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageWidth = INVENTORY_WIDTH;
        this.imageHeight = INVENTORY_HEIGHT;
        this.titleLabelX = 7;
        this.titleLabelY = 5;
        this.inventoryLabelX = 7;
        this.inventoryLabelY = 95;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        if (this.currentTab == TAB_SKILL) {
            graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, PANEL_COLOR);
        } else {
            graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        }

        this.renderTabs(graphics, x, y);

        if (this.currentTab == TAB_SKILL) {
            this.renderSkillTab(graphics, x, y);
        }
    }

    private void renderTabs(GuiGraphics graphics, int x, int y) {
        this.renderTab(graphics, x + TAB_X, y + TAB_Y, TAB_INVENTORY,
            Component.translatable("tab.aibots.inventory"));
        this.renderTab(graphics, x + TAB_X + TAB_WIDTH + 2, y + TAB_Y, TAB_SKILL,
            Component.translatable("tab.aibots.skill"));
    }

    private void renderTab(GuiGraphics graphics, int x, int y, int tab, Component label) {
        boolean active = this.currentTab == tab;
        ResourceLocation sprite = active ? TAB_SELECTED : TAB_UNSELECTED;
        graphics.blit(sprite, x, y, 0.0F, 0.0F, TAB_WIDTH, TAB_HEIGHT, TAB_WIDTH, TAB_HEIGHT);
        this.drawCenteredShadow(graphics, label, x + TAB_WIDTH / 2, y + 10, TEXT_COLOR);
    }

    private void renderSkillTab(GuiGraphics graphics, int x, int y) {
        for (AiluuSkill skill : AiluuSkill.values()) {
            int idx = skill.ordinal();
            int col = idx % 2;
            int row = idx / 2;
            int bx = x + SKILL_GRID_X + col * SKILL_COL_PITCH;
            int by = y + SKILL_GRID_Y + row * SKILL_ROW_PITCH;
            boolean selected = idx == this.menu.getSelectedSkillIndex();
            graphics.blitSprite(selected ? BUTTON_DISABLED : BUTTON, bx, by, SKILL_BTN_W, SKILL_BTN_H);
            this.drawCenteredShadow(graphics, skillName(skill), bx + SKILL_BTN_W / 2, by + 5, TEXT_COLOR);
        }
        this.renderSkillDescription(graphics, x, y);
    }

    private void renderSkillDescription(GuiGraphics graphics, int x, int y) {
        AiluuSkill selected = AiluuSkill.byOrdinal(this.menu.getSelectedSkillIndex());
        int cx = x + this.imageWidth / 2;
        int top = y + 112;
        String desc = Component.translatable(selected.descriptionKey()).getString();
        int lineY = top;
        for (String line : desc.split("\n", -1)) {
            this.drawCenteredShadow(graphics, Component.literal(line), cx, lineY, TEXT_COLOR);
            lineY += 9;
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
            if (this.inRect(x + TAB_X, y + TAB_Y, TAB_WIDTH, TAB_HEIGHT, mouseX, mouseY)) {
                this.setTab(TAB_INVENTORY);
                return true;
            }
            if (this.inRect(x + TAB_X + TAB_WIDTH + 2, y + TAB_Y, TAB_WIDTH, TAB_HEIGHT, mouseX, mouseY)) {
                this.setTab(TAB_SKILL);
                return true;
            }
            if (this.currentTab == TAB_SKILL) {
                for (AiluuSkill skill : AiluuSkill.values()) {
                    int idx = skill.ordinal();
                    int col = idx % 2;
                    int row = idx / 2;
                    int bx = x + SKILL_GRID_X + col * SKILL_COL_PITCH;
                    int by = y + SKILL_GRID_Y + row * SKILL_ROW_PITCH;
                    if (this.inRect(bx, by, SKILL_BTN_W, SKILL_BTN_H, mouseX, mouseY)) {
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
        this.updateSize();
    }

    private void updateSize() {
        if (this.currentTab == TAB_SKILL) {
            this.imageWidth = SKILL_WIDTH;
            this.imageHeight = SKILL_HEIGHT;
        } else {
            this.imageWidth = INVENTORY_WIDTH;
            this.imageHeight = INVENTORY_HEIGHT;
        }
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
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
