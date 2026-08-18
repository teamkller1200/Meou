package com.meou.entity.skill;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * Shared ambient sound pool for Meou's meows.
 */
public final class ModSounds {
    public static final SoundEvent[] MEOU_MEOWS = {
        SoundEvents.CAT_AMBIENT,
        SoundEvents.CAT_PURR,
        SoundEvents.CAT_PURREOW,
    };

    private ModSounds() {
    }
}