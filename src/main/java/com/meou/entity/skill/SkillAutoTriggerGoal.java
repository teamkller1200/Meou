package com.meou.entity.skill;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meou.Meou;
import com.meou.entity.MeouEntity;

public class SkillAutoTriggerGoal extends Goal {
    private static final Logger LOGGER = LoggerFactory.getLogger(Meou.MOD_ID);

    private final MeouEntity companion;

    public SkillAutoTriggerGoal(MeouEntity companion) {
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
        MeouSkill skill = this.companion.getSelectedSkill();
        if (skill.canTrigger(this.companion)) {
            skill.activate(this.companion);
            this.companion.setSkillCooldownTicks(skill.getCooldownTicks());
            MeouDialogue.say(this.companion, "skill." + skill.getKey());
            LOGGER.debug("[Meou {}] skill {} triggered", this.companion.getUUID(), skill.getKey());
        }
    }
}
