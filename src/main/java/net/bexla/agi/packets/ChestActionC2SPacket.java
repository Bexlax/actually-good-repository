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

public record ChestActionC2SPacket(int containerId, Action action) implements CustomPacketPayload {
    public enum Action {
        DEPOSIT_ALL,
        LOOT_ALL,
        RESTOCK
    }

    public static final Type<ChestActionC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AGIMod.MODID,
                    "chest_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChestActionC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ChestActionC2SPacket::containerId,
                    ByteBufCodecs.VAR_INT.map(
                            index -> Action.values()[index],
                            Action::ordinal
                    ),
                    ChestActionC2SPacket::action,
                    ChestActionC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(int containerId, Action action) {
        PacketDistributor.sendToServer(new ChestActionC2SPacket(containerId, action));
    }

    static void handle(ChestActionC2SPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            AbstractContainerMenu menu = player.containerMenu;
            if (menu.containerId != payload.containerId() || !menu.stillValid(player)) return;

            switch (payload.action()) {
                case DEPOSIT_ALL -> ChestTransfer.depositAll(player);
                case LOOT_ALL -> ChestTransfer.lootAll(player);
                case RESTOCK -> ChestTransfer.restock(player);
            }

            menu.broadcastChanges();
        });
    }
}