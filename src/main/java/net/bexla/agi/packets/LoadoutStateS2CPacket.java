package net.bexla.agi.packets;

import net.bexla.agi.AGIMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public record LoadoutStateS2CPacket(int selected) implements CustomPacketPayload {

    public static final Type<LoadoutStateS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AGIMod.MODID,
                    "loadout_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LoadoutStateS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    LoadoutStateS2CPacket::selected,
                    LoadoutStateS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(ServerPlayer player, int selected) {
        PacketDistributor.sendToPlayer(player, new LoadoutStateS2CPacket(selected));
    }
}