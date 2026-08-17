package com.aibots.entity.skill;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import com.aibots.entity.MeouEntity;

public enum MeouSkill {
    HEAL("heal", 40) {
        @Override
        public boolean canTrigger(MeouEntity companion) {
            Player owner = companion.getOwner();
            return owner != null
                && (owner.getHealth() <= 6.0F
                    || owner.hasEffect(MobEffects.POISON)
                    || owner.isOnFire());
        }

        @Override
        public void activate(MeouEntity companion) {
            Player owner = companion.getOwner();
            if (owner == null) {
                return;
            }
            owner.removeEffect(MobEffects.POISON);
            owner.clearFire();
            owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1));
        }
    },
    CHEER("cheer", 60) {
        @Override
        public boolean canTrigger(MeouEntity companion) {
            return hasHostileNearby(companion);
        }

        @Override
        public void activate(MeouEntity companion) {
            Player owner = companion.getOwner();
            if (owner != null) {
                owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0));
            }
        }
    },
    COLLECT("collect", 30) {
        @Override
        public boolean canTrigger(MeouEntity companion) {
            return !companion.level().getEntitiesOfClass(
                ItemEntity.class,
                companion.getBoundingBox().inflate(8.0D),
                item -> item.isAlive()
            ).isEmpty();
        }

        @Override
        public void activate(MeouEntity companion) {
            Player owner = companion.getOwner();
            if (owner == null) {
                return;
            }
            for (ItemEntity item : companion.level().getEntitiesOfClass(
                ItemEntity.class,
                companion.getBoundingBox().inflate(8.0D),
                entity -> entity.isAlive()
            )) {
                ItemStack stack = item.getItem().copy();
                owner.getInventory().add(stack);
                item.setItem(stack);
                if (stack.isEmpty()) {
                    item.discard();
                }
            }
        }
    },
    ALERT("alert", 60) {
        @Override
        public boolean canTrigger(MeouEntity companion) {
            return hasHostileNearby(companion);
        }

        @Override
        public void activate(MeouEntity companion) {
            for (LivingEntity target : hostileNearby(companion, 8.0D)) {
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
            }
        }
    },
    LIGHT("light", 60) {
        @Override
        public boolean canTrigger(MeouEntity companion) {
            return findTorchStack(companion) != null && findDarkAirBlock(companion) != null;
        }

        @Override
        public void activate(MeouEntity companion) {
            BlockPos target = findDarkAirBlock(companion);
            if (target != null) {
                if (consumeTorch(companion)) {
                    companion.level().setBlockAndUpdate(target, Blocks.TORCH.defaultBlockState());
                }
            }
        }
    },
    ATTACK("attack", 80) {
        @Override
        public boolean canTrigger(MeouEntity companion) {
            if (companion.getOwner() == null) {
                return false;
            }
            return !hostileNearby(companion, 10.0D).isEmpty();
        }

        @Override
        public void activate(MeouEntity companion) {
            List<LivingEntity> hostiles = hostileNearby(companion, 10.0D);
            LivingEntity target = null;
            double nearestSq = Double.MAX_VALUE;
            for (LivingEntity hostile : hostiles) {
                double distSq = companion.distanceToSqr(hostile);
                if (distSq < nearestSq) {
                    nearestSq = distSq;
                    target = hostile;
                }
            }
            if (target != null) {
                companion.setTarget(target);
                companion.setAttackModeTicks(100);
            }
        }
    };

    private final String key;
    private final int cooldownTicks;

    MeouSkill(String key, int cooldownTicks) {
        this.key = key;
        this.cooldownTicks = cooldownTicks;
    }

    public String getKey() {
        return this.key;
    }

    public int getCooldownTicks() {
        return this.cooldownTicks;
    }

    public String descriptionKey() {
        return "skill.aibots." + this.key + ".desc";
    }

    public abstract boolean canTrigger(MeouEntity companion);

    public abstract void activate(MeouEntity companion);

    public static MeouSkill byKey(String key) {
        for (MeouSkill skill : values()) {
            if (skill.key.equals(key)) {
                return skill;
            }
        }
        return HEAL;
    }

    public static MeouSkill byOrdinal(int ordinal) {
        MeouSkill[] skills = values();
        if (ordinal < 0 || ordinal >= skills.length) {
            return HEAL;
        }
        return skills[ordinal];
    }

    private static boolean hasHostileNearby(MeouEntity companion) {
        return !hostileNearby(companion, 8.0D).isEmpty();
    }

    private static List<LivingEntity> hostileNearby(MeouEntity companion, double radius) {
        return companion.level().getEntitiesOfClass(
            LivingEntity.class,
            companion.getBoundingBox().inflate(radius),
            entity -> entity != companion && entity.isAlive() && entity instanceof Enemy
        );
    }

    private static BlockPos findDarkAirBlock(MeouEntity companion) {
        BlockPos pos = companion.blockPosition();
        for (BlockPos candidate : BlockPos.betweenClosed(
            pos.getX() - 3, pos.getY() - 2, pos.getZ() - 3,
            pos.getX() + 3, pos.getY() + 3, pos.getZ() + 3
        )) {
            BlockState state = companion.level().getBlockState(candidate);
            BlockState belowState = companion.level().getBlockState(candidate.below());
            // 空気ブロックであり、かつ足元（下）が固体ブロック（松明を置ける）であること
            if (state.isAir() && belowState.isSolid() && companion.level().getMaxLocalRawBrightness(candidate) <= 7) {
                return candidate.immutable();
            }
        }
        return null;
    }

    private static ItemStack findTorchStack(MeouEntity companion) {
        // Meou自身のインベントリからのみ探す
        var meouInv = companion.getInventory();
        for (int i = 0; i < meouInv.getContainerSize(); i++) {
            ItemStack stack = meouInv.getItem(i);
            if (stack.is(Items.TORCH)) {
                return stack;
            }
        }
        return null;
    }

    private static boolean consumeTorch(MeouEntity companion) {
        // Meou自身のインベントリからのみ消費
        var meouInv = companion.getInventory();
        for (int i = 0; i < meouInv.getContainerSize(); i++) {
            ItemStack stack = meouInv.getItem(i);
            if (stack.is(Items.TORCH)) {
                stack.shrink(1);
                meouInv.setChanged();
                return true;
            }
        }
        return false;
    }
}
