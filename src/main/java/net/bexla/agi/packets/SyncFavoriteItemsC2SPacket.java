package net.bexla.agi.packets;

import net.bexla.agi.AGIMod;
import net.bexla.agi.content.FavoriteItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncFavoriteItemsC2SPacket(int containerId, int slotId, boolean favoriteStatus) implements CustomPacketPayload {

    public static final Type<SyncFavoriteItemsC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AGIMod.MODID,
                    "sync_favorite_items"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncFavoriteItemsC2SPacket> STREAM_CODEC = StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    SyncFavoriteItemsC2SPacket::containerId,
                    ByteBufCodecs.VAR_INT,
                    SyncFavoriteItemsC2SPacket::slotId,
                    ByteBufCodecs.BOOL,
                    SyncFavoriteItemsC2SPacket::favoriteStatus,
                    SyncFavoriteItemsC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(int containerId, int slotId, boolean favoriteStatus) {
        PacketDistributor.sendToServer(new SyncFavoriteItemsC2SPacket(containerId, slotId, favoriteStatus));
    }

    static void handle(SyncFavoriteItemsC2SPacket payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;

        AbstractContainerMenu menu = player.containerMenu;

        if (payload.containerId() != menu.containerId) {
            AGIMod.LOGGER.warn("Ignoring favorite update for mismatching container. " + "Packet: {}, player: {}.", payload.containerId(), menu.containerId);
            return;
        }

        if (payload.slotId() < 0 || payload.slotId() >= menu.slots.size()) return;

        Slot slot = menu.slots.get(payload.slotId());
        if (slot.container != player.getInventory()) return;

        ItemStack stack = slot.getItem();
        if (stack.isEmpty()) return;

        FavoriteItems.setFavorite(stack, payload.favoriteStatus());
        AGIMod.LOGGER.info("Favorite component after update: {}", stack.get(AGIMod.FAVORITE.get()));

        menu.broadcastChanges();
    }
}