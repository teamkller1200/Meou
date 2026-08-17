package com.meou.screen;

import com.meou.Meou;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RenamePayload(int entityId, String name) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RenamePayload> TYPE =
        new CustomPacketPayload.Type<>(Meou.id("rename"));

    public static final StreamCodec<FriendlyByteBuf, RenamePayload> CODEC =
        StreamCodec.ofMember(RenamePayload::write, RenamePayload::new);

    private RenamePayload(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readUtf(64));
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeUtf(this.name, 64);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
