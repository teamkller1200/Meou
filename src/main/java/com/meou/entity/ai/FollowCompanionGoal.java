package com.meou.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;

import java.util.EnumSet;

import org.jetbrains.annotations.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.world.phys.Vec3;

import com.meou.Meou;
import com.meou.entity.MeouEntity;

/**
 * Keeps the companion in a stable formation orbit around the owner.
 * The angle is derived from the companion UUID so multiple companions do not
 * stack on top of each other.
 */
public class FollowCompanionGoal extends Goal {
    private static final Logger LOGGER = LoggerFactory.getLogger(Meou.MOD_ID);

    private static final double FORMATION_RADIUS = 5.0D;
    private static final double FULL_CIRCLE = 2.0D * Math.PI;

    private final MeouEntity companion;
    private final double teleportDistance;
    private final double formationAngle;
    @Nullable
    private LivingEntity owner;
    private int timeToRecalcPath;
    private float oldWaterCost;

    public FollowCompanionGoal(MeouEntity companion, double followDistance, double teleportDistance) {
        this.companion = companion;
        this.teleportDistance = teleportDistance;
        this.formationAngle = deriveFormationAngle(companion);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    /**
     * Deterministic pseudo-random offset based on entity UUID.
     * This keeps each companion in a different orbit slot around the owner.
     */
    private static double deriveFormationAngle(MeouEntity companion) {
        long bits = companion.getUUID().getMostSignificantBits();
        long h = bits * 0x9E3779B97F4A7C15L;
        h ^= h >>> 32;
        double ratio = (h & Long.MAX_VALUE) / (double) Long.MAX_VALUE;
        return ratio * FULL_CIRCLE;
    }

    private Vec3 targetAroundOwner() {
        if (this.owner == null) {
            return null;
        }
        Vec3 pos = this.owner.position();
        return pos.add(
                Math.cos(this.formationAngle) * FORMATION_RADIUS,
                0.0D,
                Math.sin(this.formationAngle) * FORMATION_RADIUS);
    }

    @Override
    public boolean canUse() {
        this.owner = this.companion.getOwner();
        if (this.owner == null) {
            return false;
        }
        Vec3 target = this.targetAroundOwner();
        return target != null && this.companion.distanceToSqr(target.x, target.y, target.z) > 6.25D;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.owner == null) {
            return false;
        }
        Vec3 target = this.targetAroundOwner();
        return target != null && this.companion.distanceToSqr(target.x, target.y, target.z) > 6.25D;
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

        double ownerDistSqr = this.companion.distanceToSqr(this.owner);
        if (ownerDistSqr > this.teleportDistance * this.teleportDistance) {
            this.companion.teleportToOwner(this.owner);
            this.tryTeleportToNear(this.companion, this.owner);
            com.meou.entity.skill.MeouDialogue.say(this.companion, "teleport");
            return;
        }

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            Vec3 target = this.targetAroundOwner();
            if (target != null && this.companion.distanceToSqr(target.x, target.y, target.z) > 6.25D) {
                PathNavigation navigation = this.companion.getNavigation();
                navigation.moveTo(target.x, target.y, target.z, 1.0D);
            }
        }
    }

    private void tryTeleportToNear(MeouEntity companion, LivingEntity owner) {
        Vec3 target = this.targetAroundOwner();
        if (target == null) {
            return;
        }
        PathNavigation navigation = companion.getNavigation();
        navigation.moveTo(target.x, target.y, target.z, 1.0D);
    }
}