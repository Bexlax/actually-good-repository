package net.bexla.agi.packets;

import net.bexla.agi.AGIMod;
import net.bexla.agi.content.LoadoutClientState;
import net.bexla.agi.content.RecentItems;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = AGIMod.MODID)
public class RegisterPayloadHandlers {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(QuickStackC2SPacket.TYPE, QuickStackC2SPacket.STREAM_CODEC,
                QuickStackC2SPacket::handle);

        registrar.playToClient(RecentItemS2CPacket.TYPE, RecentItemS2CPacket.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> RecentItems.mark(payload.slot())));

        registrar.playToServer(LoadoutSelectC2SPacket.TYPE, LoadoutSelectC2SPacket.STREAM_CODEC,
                LoadoutSelectC2SPacket::handle);

        registrar.playToClient(LoadoutStateS2CPacket.TYPE, LoadoutStateS2CPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(
                        () -> LoadoutClientState.setSelected(packet.selected())));

        registrar.playToServer(SimilarStackC2SPacket.TYPE, SimilarStackC2SPacket.STREAM_CODEC,
                SimilarStackC2SPacket::handle);

        registrar.playToServer(ChestActionC2SPacket.TYPE, ChestActionC2SPacket.STREAM_CODEC,
                ChestActionC2SPacket::handle);

        registrar.playToServer(SortInventoryC2SPacket.TYPE, SortInventoryC2SPacket.STREAM_CODEC,
                SortInventoryC2SPacket::handle);

        registrar.playToServer(SyncFavoriteItemsC2SPacket.TYPE, SyncFavoriteItemsC2SPacket.STREAM_CODEC,
                SyncFavoriteItemsC2SPacket::handle);
    }
}
