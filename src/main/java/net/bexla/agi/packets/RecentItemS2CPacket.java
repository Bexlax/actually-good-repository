package net.bexla.agi.packets;

import net.bexla.agi.AGIMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public record RecentItemS2CPacket(int slot) implements CustomPacketPayload {

    public static final Type<RecentItemS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AGIMod.MODID,
                    "recent_item"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RecentItemS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    RecentItemS2CPacket::slot,
                    RecentItemS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(ServerPlayer player, int slot) {
        PacketDistributor.sendToPlayer(player, new RecentItemS2CPacket(slot));
    }
}