package com.meou.client.screen;

import com.meou.Meou;
import com.meou.entity.skill.MeouSkill;
import com.meou.screen.MeouScreenHandler;
import com.meou.screen.RenamePayload;
import com.meou.screen.SkillSelectPayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class MeouScreen extends AbstractContainerScreen<MeouScreenHandler> {
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(Meou.MOD_ID, "textures/gui/container/meou.png");
    private static final ResourceLocation TAB_SELECTED =
        ResourceLocation.fromNamespaceAndPath(Meou.MOD_ID, "textures/gui/meou_tab_selected.png");
    private static final ResourceLocation TAB_UNSELECTED =
        ResourceLocation.fromNamespaceAndPath(Meou.MOD_ID, "textures/gui/meou_tab_unselected.png");

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

    private static final int RENAME_LABEL_Y = 132;
    private static final int RENAME_EDIT_X = 12;
    private static final int RENAME_EDIT_Y = 142;
    private static final int RENAME_EDIT_W = 100;
    private static final int RENAME_EDIT_H = 18;
    private static final int RENAME_BTN_X = 118;
    private static final int RENAME_BTN_Y = 141;
    private static final int RENAME_BTN_W = 50;
    private static final int RENAME_BTN_H = 18;

    private int currentTab = TAB_INVENTORY;
    private EditBox nameEditBox;
    private Button renameButton;

    public MeouScreen(MeouScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageWidth = INVENTORY_WIDTH;
        this.imageHeight = INVENTORY_HEIGHT;
        this.titleLabelX = 7;
        this.titleLabelY = 5;
        this.inventoryLabelX = 7;
        this.inventoryLabelY = 95;
    }

    @Override
    protected void init() {
        super.init();
        this.nameEditBox = new EditBox(this.font, 0, 0, RENAME_EDIT_W, RENAME_EDIT_H, Component.literal(""));
        this.nameEditBox.setMaxLength(32);
        this.nameEditBox.setHint(Component.translatable("rename.meou.hint"));
        this.addRenderableWidget(this.nameEditBox);

        this.renameButton = Button.builder(
            Component.translatable("rename.meou.button"),
            button -> this.applyRename()
        ).bounds(0, 0, RENAME_BTN_W, RENAME_BTN_H).build();
        this.addRenderableWidget(this.renameButton);

        this.layoutRename();
    }

    private void applyRename() {
        String name = this.nameEditBox.getValue().trim();
        if (!name.isEmpty()) {
            ClientPlayNetworking.send(new RenamePayload(this.menu.getEntityId(), name));
        }
        this.nameEditBox.setFocused(false);
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
            Component.translatable("tab.meou.inventory"));
        this.renderTab(graphics, x + TAB_X + TAB_WIDTH + 2, y + TAB_Y, TAB_SKILL,
            Component.translatable("tab.meou.skill"));
    }

    private void renderTab(GuiGraphics graphics, int x, int y, int tab, Component label) {
        boolean active = this.currentTab == tab;
        ResourceLocation sprite = active ? TAB_SELECTED : TAB_UNSELECTED;
        graphics.blit(sprite, x, y, 0.0F, 0.0F, TAB_WIDTH, TAB_HEIGHT, TAB_WIDTH, TAB_HEIGHT);
        this.drawCenteredShadow(graphics, label, x + TAB_WIDTH / 2, y + 10, TEXT_COLOR);
    }

    private void renderSkillTab(GuiGraphics graphics, int x, int y) {
        for (MeouSkill skill : MeouSkill.values()) {
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
        MeouSkill selected = MeouSkill.byOrdinal(this.menu.getSelectedSkillIndex());
        int cx = x + this.imageWidth / 2;
        int top = y + 112;
        String desc = Component.translatable(selected.descriptionKey()).getString();
        int lineY = top;
        for (String line : desc.split("\n", -1)) {
            this.drawCenteredShadow(graphics, Component.literal(line), cx, lineY, TEXT_COLOR);
            lineY += 9;
        }
        this.drawCenteredShadow(graphics, Component.translatable("rename.meou.label"),
            x + RENAME_EDIT_X + RENAME_EDIT_W / 2, y + RENAME_LABEL_Y, TEXT_COLOR);
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
                for (MeouSkill skill : MeouSkill.values()) {
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
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.nameEditBox != null && this.nameEditBox.isFocused()
            && this.nameEditBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.nameEditBox != null && this.nameEditBox.isFocused()
            && this.nameEditBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void setTab(int tab) {
        this.currentTab = tab;
        this.menu.setSkillTab(tab == TAB_SKILL);
        this.updateSize();
        this.layoutRename();
    }

    private void layoutRename() {
        boolean skill = this.currentTab == TAB_SKILL;
        this.nameEditBox.setPosition(this.leftPos + RENAME_EDIT_X, this.topPos + RENAME_EDIT_Y);
        this.nameEditBox.setVisible(skill);
        this.renameButton.setPosition(this.leftPos + RENAME_BTN_X, this.topPos + RENAME_BTN_Y);
        this.renameButton.visible = skill;
        if (!skill) {
            this.nameEditBox.setFocused(false);
        }
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

    private void selectSkill(MeouSkill skill) {
        ClientPlayNetworking.send(new SkillSelectPayload(this.menu.getEntityId(), skill.ordinal()));
    }

    private static Component skillName(MeouSkill skill) {
        return Component.translatable("skill.meou." + skill.getKey());
    }

    private boolean inRect(int bx, int by, int w, int h, double mouseX, double mouseY) {
        return mouseX >= bx && mouseX < bx + w && mouseY >= by && mouseY < by + h;
    }

    private void drawCenteredShadow(GuiGraphics graphics, Component text, int centerX, int y, int color) {
        graphics.drawString(this.font, text, centerX - this.font.width(text) / 2, y, color, true);
    }
}
