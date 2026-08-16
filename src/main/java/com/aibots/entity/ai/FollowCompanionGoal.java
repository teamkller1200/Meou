package com.aibots.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;

import java.util.EnumSet;

import org.jetbrains.annotations.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aibots.Aibots;
import com.aibots.entity.TestEntity;

public class FollowCompanionGoal extends Goal {
    private static final Logger LOGGER = LoggerFactory.getLogger(Aibots.MOD_ID);

    private final TestEntity companion;
    private final double followDistance;
    private final double teleportDistance;
    @Nullable
    private LivingEntity owner;
    private int timeToRecalcPath;
    private float oldWaterCost;

    public FollowCompanionGoal(TestEntity companion, double followDistance, double teleportDistance) {
        this.companion = companion;
        this.followDistance = followDistance;
        this.teleportDistance = teleportDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.owner = this.companion.getOwner();
        if (this.owner == null) {
            return false;
        }
        return this.companion.distanceToSqr(this.owner) > 6.25D;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.owner == null) {
            return false;
        }
        return this.companion.distanceToSqr(this.owner) > 6.25D;
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.companion.getPathfindingMalus(PathType.WATER);
        this.companion.setPathfindingMalus(PathType.WATER, 0.0F);
        LOGGER.debug("[FollowCompanionGoal {}] start following {}", this.companion.getUUID(), this.owner);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.companion.getNavigation().stop();
        this.companion.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        if (this.owner == null) {
            return;
        }
        this.companion.getLookControl().setLookAt(this.owner, 10.0F, (float) this.companion.getMaxHeadXRot());

        double distSqr = this.companion.distanceToSqr(this.owner);
        if (distSqr > this.teleportDistance * this.teleportDistance) {
            this.companion.teleportToOwner(this.owner);
            this.tryTeleportToNear(this.companion, this.owner);
            return;
        }

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            if (distSqr > this.followDistance * this.followDistance && distSqr > 6.25D) {
                PathNavigation navigation = this.companion.getNavigation();
                navigation.moveTo(this.owner, 1.0D);
            }
        }
    }

    private void tryTeleportToNear(TestEntity companion, LivingEntity owner) {
        PathNavigation navigation = companion.getNavigation();
        navigation.moveTo(owner, 1.0D);
    }
}