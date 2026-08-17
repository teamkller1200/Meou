package com.aibots.screen;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.aibots.entity.AiluuEntity;

public class AiluuScreenHandler extends AbstractContainerMenu {
    private static final int PLAYER_INV_X = 7;
    private static final int PLAYER_INV_Y = 101;
    private static final int HOTBAR_Y = 159;
    private static final int HAND_SLOT_X = 79;
    private static final int HAND_SLOT_Y = 17;
    private static final int STORAGE_X = 7;
    private static final int STORAGE_Y = 39;
    private static final int SLOT_SIZE = 18;
    private static final int COLUMNS = 9;

    private final Container inventory;
    private final DataSlot entityIdDataSlot;
    private final DataSlot selectedSkillDataSlot;
    private boolean skillTab;

    public AiluuScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(AiluuEntity.TOTAL_SLOTS));
    }

    public AiluuScreenHandler(int syncId, Inventory playerInventory, Container inventory) {
        this(syncId, playerInventory, inventory, null);
    }

    public AiluuScreenHandler(int syncId, Inventory playerInventory, Container inventory, AiluuEntity entity) {
        super(ModMenuTypes.AILUU, syncId);
        this.inventory = inventory;
        this.skillTab = false;

        if (entity != null) {
            this.entityIdDataSlot = this.addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    return entity.getId();
                }

                @Override
                public void set(int value) {
                }
            });
            this.selectedSkillDataSlot = this.addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    return entity.getSelectedSkill().ordinal();
                }

                @Override
                public void set(int value) {
                }
            });
        } else {
            this.entityIdDataSlot = this.addDataSlot(DataSlot.standalone());
            this.selectedSkillDataSlot = this.addDataSlot(DataSlot.standalone());
        }

        checkContainerSize(inventory, AiluuEntity.TOTAL_SLOTS);
        inventory.startOpen(playerInventory.player);

        this.addSlot(new Slot(inventory, AiluuEntity.HAND_SLOT, HAND_SLOT_X, HAND_SLOT_Y) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean isActive() {
                return !AiluuScreenHandler.this.skillTab;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                int index = 1 + row * COLUMNS + col;
                this.addSlot(this.newInactiveAwareSlot(inventory, index, STORAGE_X + col * SLOT_SIZE, STORAGE_Y + row * SLOT_SIZE));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(this.newInactiveAwareSlot(playerInventory, 9 + row * 9 + col, PLAYER_INV_X + col * SLOT_SIZE, PLAYER_INV_Y + row * SLOT_SIZE));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(this.newInactiveAwareSlot(playerInventory, col, PLAYER_INV_X + col * SLOT_SIZE, HOTBAR_Y));
        }
    }

    private Slot newInactiveAwareSlot(Container container, int index, int x, int y) {
        return new Slot(container, index, x, y) {
            @Override
            public boolean isActive() {
                return !AiluuScreenHandler.this.skillTab;
            }
        };
    }

    public void setSkillTab(boolean skillTab) {
        this.skillTab = skillTab;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (slotIndex < AiluuEntity.TOTAL_SLOTS) {
                if (!this.moveItemStackTo(stack, AiluuEntity.TOTAL_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, AiluuEntity.TOTAL_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    public Container getInventory() {
        return this.inventory;
    }

    public int getEntityId() {
        return this.entityIdDataSlot.get();
    }

    public int getSelectedSkillIndex() {
        return this.selectedSkillDataSlot.get();
    }
}
