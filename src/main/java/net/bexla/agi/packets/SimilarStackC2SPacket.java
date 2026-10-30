package net.bexla.agi.packets;

import net.bexla.agi.AGIMod;
import net.bexla.agi.content.ChestTransfer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SimilarStackC2SPacket(int containerId) implements CustomPacketPayload {

    public static final Type<SimilarStackC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AGIMod.MODID, "similar_stack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SimilarStackC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    SimilarStackC2SPacket::containerId,
                    SimilarStackC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(int containerId) {
        PacketDistributor.sendToServer(new SimilarStackC2SPacket(containerId));
    }

    static void handle(SimilarStackC2SPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            AbstractContainerMenu menu = player.containerMenu;
            if (menu.containerId != payload.containerId() || !menu.stillValid(player)) return;

            ChestTransfer.similarStack(player);
            menu.broadcastChanges();
        });
    }
}