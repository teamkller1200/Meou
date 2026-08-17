package com.aibots.entity.skill;

import java.util.Map;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import com.aibots.entity.MeouEntity;

public final class MeouDialogue {
    private static final Map<String, Integer> LINE_COUNTS = Map.of(
        "skill.heal", 3,
        "skill.cheer", 2,
        "skill.collect", 2,
        "skill.alert", 2,
        "skill.light", 2,
        "skill.attack", 3,
        "teleport", 2,
        "death", 4
    );

    private MeouDialogue() {
    }

    public static void say(MeouEntity companion, String prefix) {
        if (companion.level().isClientSide) {
            return;
        }
        Player owner = companion.getOwner();
        if (owner == null) {
            return;
        }
        Integer count = LINE_COUNTS.get(prefix);
        if (count == null || count <= 0) {
            return;
        }
        if (companion.tickCount < companion.getLastDialogueTick() + companion.getDialogueInterval()) {
            return;
        }
        companion.setLastDialogueTick(companion.tickCount);
        sendLine(companion, prefix, count);
    }

    public static void sayDeath(MeouEntity companion) {
        if (companion.level().isClientSide) {
            return;
        }
        Integer count = LINE_COUNTS.get("death");
        if (count == null || count <= 0) {
            return;
        }
        // 死亡時は単発なのでスパム防止チェックを適用しない
        sendLine(companion, "death", count);
    }

    private static void sendLine(MeouEntity companion, String prefix, int count) {
        Player owner = companion.getOwner();
        if (owner == null) {
            return;
        }
        int line = companion.getRandom().nextInt(count) + 1;
        String key = "dialogue.aibots." + prefix + "." + line;
        String name = companion.getName().getString();
        owner.sendSystemMessage(Component.literal("[" + name + "] ").append(Component.translatable(key)));
    }
}
