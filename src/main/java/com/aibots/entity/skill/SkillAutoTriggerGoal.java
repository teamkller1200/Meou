package com.aibots.entity.skill;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aibots.Aibots;
import com.aibots.entity.AiluuEntity;

public class SkillAutoTriggerGoal extends Goal {
    private static final Logger LOGGER = LoggerFactory.getLogger(Aibots.MOD_ID);

    private final AiluuEntity companion;

    public SkillAutoTriggerGoal(AiluuEntity companion) {
        this.companion = companion;
        this.setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    @Override
    public boolean canUse() {
        return this.companion.hasOwner();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void tick() {
        if (this.companion.getSkillCooldownTicks() > 0) {
            this.companion.setSkillCooldownTicks(this.companion.getSkillCooldownTicks() - 1);
            return;
        }
        AiluuSkill skill = this.companion.getSelectedSkill();
        if (skill.canTrigger(this.companion)) {
            skill.activate(this.companion);
            this.companion.setSkillCooldownTicks(skill.getCooldownTicks());
            LOGGER.debug("[Ailuu {}] skill {} triggered", this.companion.getUUID(), skill.getKey());
        }
    }
}
