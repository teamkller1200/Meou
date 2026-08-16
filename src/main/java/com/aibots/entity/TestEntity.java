package com.aibots.entity;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aibots.Aibots;
import com.aibots.entity.ai.FollowCompanionGoal;

public class TestEntity extends PathfinderMob {
    private static final Logger LOGGER = LoggerFactory.getLogger(Aibots.MOD_ID);

    @Nullable
    private UUID ownerId;

    public TestEntity(EntityType<? extends TestEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D)
            .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FollowCompanionGoal(this, 2.5D, 32.0D));
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
                LOGGER.debug("[TestEntity {}] ownerId set but owner player not found (offline?)", this.getUUID());
            }
            return owner;
        }
        return null;
    }

    public void setOwner(Player player) {
        this.ownerId = player.getUUID();
        LOGGER.info("[TestEntity {}] owner set to {}", this.getUUID(), this.ownerId);
    }

    public boolean hasOwner() {
        return this.ownerId != null;
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
            LOGGER.info("[TestEntity {}] already has owner {}: skip auto assign", this.getUUID(), this.ownerId);
            return true;
        }
        Player nearest = this.level().getNearestPlayer(this, 16.0D);
        if (nearest != null) {
            this.ownerId = nearest.getUUID();
            LOGGER.info("[TestEntity {}] auto-assigned owner {}", this.getUUID(), this.ownerId);
            return true;
        }
        LOGGER.info("[TestEntity {}] no nearby player within 16 blocks to assign owner", this.getUUID());
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (this.ownerId != null) {
            nbt.putUUID("Owner", this.ownerId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("Owner")) {
            this.ownerId = nbt.getUUID("Owner");
        }
    }
}