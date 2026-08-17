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

import com.aibots.entity.AiluuEntity;

public enum AiluuSkill {
    HEAL("heal", 200) {
        @Override
        public boolean canTrigger(AiluuEntity companion) {
            Player owner = companion.getOwner();
            return owner != null && owner.getHealth() <= 6.0F;
        }

        @Override
        public void activate(AiluuEntity companion) {
            Player owner = companion.getOwner();
            if (owner != null) {
                owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1));
            }
        }
    },
    CHEER("cheer", 300) {
        @Override
        public boolean canTrigger(AiluuEntity companion) {
            return hasHostileNearby(companion);
        }

        @Override
        public void activate(AiluuEntity companion) {
            Player owner = companion.getOwner();
            if (owner != null) {
                owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0));
            }
        }
    },
    COLLECT("collect", 100) {
        @Override
        public boolean canTrigger(AiluuEntity companion) {
            return !companion.level().getEntitiesOfClass(
                ItemEntity.class,
                companion.getBoundingBox().inflate(8.0D),
                item -> item.isAlive()
            ).isEmpty();
        }

        @Override
        public void activate(AiluuEntity companion) {
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
    ALERT("alert", 200) {
        @Override
        public boolean canTrigger(AiluuEntity companion) {
            return hasHostileNearby(companion);
        }

        @Override
        public void activate(AiluuEntity companion) {
            for (LivingEntity target : hostileNearby(companion)) {
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
            }
        }
    },
    LIGHT("light", 200) {
        @Override
        public boolean canTrigger(AiluuEntity companion) {
            Player owner = companion.getOwner();
            if (owner == null || owner.getInventory().countItem(Items.TORCH) <= 0) {
                return false;
            }
            return findDarkAirBlock(companion) != null;
        }

        @Override
        public void activate(AiluuEntity companion) {
            Player owner = companion.getOwner();
            if (owner == null) {
                return;
            }
            BlockPos target = findDarkAirBlock(companion);
            if (target != null) {
                companion.level().setBlockAndUpdate(target, Blocks.TORCH.defaultBlockState());
                consumeTorch(owner);
            }
        }
    };

    private final String key;
    private final int cooldownTicks;

    AiluuSkill(String key, int cooldownTicks) {
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

    public abstract boolean canTrigger(AiluuEntity companion);

    public abstract void activate(AiluuEntity companion);

    public static AiluuSkill byKey(String key) {
        for (AiluuSkill skill : values()) {
            if (skill.key.equals(key)) {
                return skill;
            }
        }
        return HEAL;
    }

    public static AiluuSkill byOrdinal(int ordinal) {
        AiluuSkill[] skills = values();
        if (ordinal < 0 || ordinal >= skills.length) {
            return HEAL;
        }
        return skills[ordinal];
    }

    private static boolean hasHostileNearby(AiluuEntity companion) {
        return !hostileNearby(companion).isEmpty();
    }

    private static List<LivingEntity> hostileNearby(AiluuEntity companion) {
        return companion.level().getEntitiesOfClass(
            LivingEntity.class,
            companion.getBoundingBox().inflate(8.0D),
            entity -> entity != companion && entity.isAlive() && entity instanceof Enemy
        );
    }

    private static BlockPos findDarkAirBlock(AiluuEntity companion) {
        BlockPos pos = companion.blockPosition();
        for (BlockPos candidate : BlockPos.betweenClosed(
            pos.getX() - 3, pos.getY() - 2, pos.getZ() - 3,
            pos.getX() + 3, pos.getY() + 3, pos.getZ() + 3
        )) {
            BlockState state = companion.level().getBlockState(candidate);
            if (state.isAir() && companion.level().getMaxLocalRawBrightness(candidate) <= 7) {
                return candidate.immutable();
            }
        }
        return null;
    }

    private static boolean consumeTorch(Player player) {
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(Items.TORCH)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }
}
