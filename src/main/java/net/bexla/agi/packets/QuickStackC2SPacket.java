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

public record QuickStackC2SPacket(int containerId) implements CustomPacketPayload {

    public static final Type<QuickStackC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AGIMod.MODID, "quick_stack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuickStackC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    QuickStackC2SPacket::containerId,
                    QuickStackC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(int containerId) {
        PacketDistributor.sendToServer(new QuickStackC2SPacket(containerId));
    }

    static void handle(QuickStackC2SPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            AbstractContainerMenu menu = player.containerMenu;
            if (menu.containerId != payload.containerId() || !menu.stillValid(player)) return;

            ChestTransfer.quickStack(player);
            menu.broadcastChanges();
        });
    }
}