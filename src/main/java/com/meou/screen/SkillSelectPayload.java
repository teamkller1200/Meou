package com.meou.screen;

import com.meou.Meou;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Payload for switching the selected skill on a Meou instance.
 */
public record SkillSelectPayload(int entityId, int skillOrdinal) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SkillSelectPayload> TYPE = new CustomPacketPayload.Type<>(
            Meou.id("skill_select"));

    public static final StreamCodec<FriendlyByteBuf, SkillSelectPayload> CODEC = StreamCodec.ofMember(
            SkillSelectPayload::write,
            SkillSelectPayload::new);

    private SkillSelectPayload(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.skillOrdinal);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
