package com.meou.entity;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meou.Meou;
import com.meou.entity.ai.FollowCompanionGoal;
import com.meou.entity.skill.MeouDialogue;
import com.meou.entity.skill.MeouSkill;
import com.meou.entity.skill.ModSounds;
import com.meou.entity.skill.SkillAutoTriggerGoal;
import com.meou.screen.MeouScreenHandler;

public class MeouEntity extends PathfinderMob {
    private static final Logger LOGGER = LoggerFactory.getLogger(Meou.MOD_ID);

    public static final int HAND_SLOT = 0;
    public static final int STORAGE_SLOTS = 27;
    public static final int TOTAL_SLOTS = 1 + STORAGE_SLOTS;

    private static final int UNOWNED_DESPAWN_TICKS = 100;
    private static final int MEOW_INTERVAL_MIN = 300;
    private static final int MEOW_INTERVAL_MAX = 900;

    @Nullable
    private UUID ownerId;

    private int unownedTicks;

    private MeouSkill selectedSkill = MeouSkill.HEAL;
    private int skillCooldownTicks;
    private int attackModeTicks;
    private int lastDialogueTick;
    private int nextMumbleTick;
    private int nextMeowTick;
    private ItemStack lastHeldItem = ItemStack.EMPTY;

    private final SimpleContainer inventory = new SimpleContainer(TOTAL_SLOTS);

    public MeouEntity(EntityType<? extends MeouEntity> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.32D)
            .add(Attributes.FOLLOW_RANGE, 48.0D)
            .add(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false) {
            @Override
            public boolean canUse() {
                return MeouEntity.this.attackModeTicks > 0 && super.canUse();
            }
        });
        this.goalSelector.addGoal(2, new FollowCompanionGoal(this, 2.5D, 32.0D));
        this.goalSelector.addGoal(3, new SkillAutoTriggerGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        if (this.ownerId == null) {
            if (++this.unownedTicks >= UNOWNED_DESPAWN_TICKS) {
                this.discard();
            }
        } else {
            this.unownedTicks = 0;
        }
        if (this.attackModeTicks > 0) {
            this.attackModeTicks--;
            if (this.attackModeTicks == 0) {
                this.setTarget(null);
            }
        }
        this.updateHeldAttackDamage();
        this.tryMumble();
        this.tryMeow();
    }

    private void tryMeow() {
        if (this.nextMeowTick == 0) {
            this.nextMeowTick = this.tickCount + this.getRandom().nextIntBetweenInclusive(MEOW_INTERVAL_MIN, MEOW_INTERVAL_MAX);
        }
        if (this.tickCount < this.nextMeowTick) {
            return;
        }
        SoundEvent[] meows = ModSounds.MEOU_MEOWS;
        if (meows.length > 0) {
            float pitch = 0.8F + this.getRandom().nextFloat() * 0.4F;
            this.playSound(meows[this.getRandom().nextInt(meows.length)], 1.0F, pitch);
        }
        this.nextMeowTick = this.tickCount + this.getRandom().nextIntBetweenInclusive(MEOW_INTERVAL_MIN, MEOW_INTERVAL_MAX);
    }

    private void tryMumble() {
        if (this.nextMumbleTick == 0) {
            this.nextMumbleTick = this.tickCount + this.getRandom().nextIntBetweenInclusive(600, 1200);
        }
        if (this.tickCount < this.nextMumbleTick) {
            return;
        }
        MeouDialogue.sayMumble(this);
        this.nextMumbleTick = this.tickCount + this.getRandom().nextIntBetweenInclusive(600, 1200);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        MeouDialogue.sayDeath(this);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.tryAutoAssignOwner();
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Nullable
    public Player getOwner() {
        if (this.ownerId == null) {
            return null;
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            Player owner = serverLevel.getServer().getPlayerList().getPlayer(this.ownerId);
            if (owner == null) {
                LOGGER.debug("[Meou {}] ownerId set but owner player not found (offline?)", this.getUUID());
            }
            return owner;
        }
        return null;
    }

    public void setOwner(Player player) {
        this.ownerId = player.getUUID();
        LOGGER.info("[Meou {}] owner set to {}", this.getUUID(), this.ownerId);
    }

    public boolean hasOwner() {
        return this.ownerId != null;
    }

    public MeouSkill getSelectedSkill() {
        return this.selectedSkill;
    }

    public void setSelectedSkill(MeouSkill skill) {
        this.selectedSkill = skill;
    }

    public int getSkillCooldownTicks() {
        return this.skillCooldownTicks;
    }

    public void setSkillCooldownTicks(int ticks) {
        this.skillCooldownTicks = Math.max(0, ticks);
    }

    public void setAttackModeTicks(int ticks) {
        this.attackModeTicks = Math.max(0, ticks);
    }

    public int getLastDialogueTick() {
        return this.lastDialogueTick;
    }

    public void setLastDialogueTick(int tick) {
        this.lastDialogueTick = tick;
    }

    public int getDialogueInterval() {
        return 60;
    }

    private void updateHeldAttackDamage() {
        ItemStack held = this.inventory.getItem(HAND_SLOT);
        if (ItemStack.isSameItemSameComponents(held, this.lastHeldItem)) {
            return;
        }
        this.lastHeldItem = held.copy();
        var attribute = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attribute != null) {
            attribute.setBaseValue(getHeldItemAttackDamage(held));
        }
    }

    private static double getHeldItemAttackDamage(ItemStack stack) {
        double[] damage = {1.0D};
        if (!stack.isEmpty()) {
            stack.forEachModifier(EquipmentSlot.MAINHAND, (holder, modifier) -> {
                if (holder.is(Attributes.ATTACK_DAMAGE)) {
                    damage[0] = modifier.amount();
                }
            });
        }
        return damage[0];
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide) {
            if (player.isShiftKeyDown() && player == getOwner()) {
                player.openMenu(new SimpleMenuProvider(
                    (syncId, inv, p) -> new MeouScreenHandler(syncId, inv, this.inventory, this),
                    Component.translatable("container.meou.meou")
                ));
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    public void teleportToOwner(LivingEntity owner) {
        Vec3 pos = owner.position();
        this.moveTo(
            pos.x,
            pos.y + owner.getBbHeight(),
            pos.z,
            owner.getYRot(),
            0.0F
        );
    }

    public boolean tryAutoAssignOwner() {
        if (this.ownerId != null) {
            LOGGER.info("[Meou {}] already has owner {}: skip auto assign", this.getUUID(), this.ownerId);
            return true;
        }
        Player nearest = this.level().getNearestPlayer(this, 16.0D);
        if (nearest != null) {
            this.ownerId = nearest.getUUID();
            LOGGER.info("[Meou {}] auto-assigned owner {}", this.getUUID(), this.ownerId);
            return true;
        }
        LOGGER.info("[Meou {}] no nearby player within 16 blocks to assign owner", this.getUUID());
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (this.ownerId != null) {
            nbt.putUUID("Owner", this.ownerId);
        }
        nbt.putString("SelectedSkill", this.selectedSkill.getKey());
        nbt.putInt("SkillCooldown", this.skillCooldownTicks);
        ContainerHelper.saveAllItems(nbt, this.inventory.getItems(), this.registryAccess());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("Owner")) {
            this.ownerId = nbt.getUUID("Owner");
        }
        this.selectedSkill = MeouSkill.byKey(nbt.getString("SelectedSkill"));
        this.skillCooldownTicks = nbt.getInt("SkillCooldown");
        ContainerHelper.loadAllItems(nbt, this.inventory.getItems(), this.registryAccess());
    }
}