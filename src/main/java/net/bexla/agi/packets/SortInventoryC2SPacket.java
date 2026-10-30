package net.bexla.agi.packets;

import net.bexla.agi.AGIMod;
import net.bexla.agi.content.InventorySorter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public record SortInventoryC2SPacket(int containerId) implements CustomPacketPayload {

    public static final Type<SortInventoryC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AGIMod.MODID,
                    "sort_inventory"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SortInventoryC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SortInventoryC2SPacket::containerId,
            SortInventoryC2SPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(int containerId) {
        PacketDistributor.sendToServer(new SortInventoryC2SPacket(containerId));
    }

    static void handle(SortInventoryC2SPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            AbstractContainerMenu menu = player.containerMenu;

            if (menu.containerId != payload.containerId()) return;
            if (!menu.stillValid(player)) return;

            InventorySorter.sort(player);
            menu.broadcastChanges();
        });
    }
}