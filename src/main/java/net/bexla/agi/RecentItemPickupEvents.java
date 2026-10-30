package net.bexla.agi;

import net.bexla.agi.packets.RecentItemS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = AGIMod.MODID)
public final class RecentItemPickupEvents {
    private static final Map<UUID, PickupSnapshot> SNAPSHOTS = new HashMap<>();

    private RecentItemPickupEvents() {}

    @SubscribeEvent
    public static void onPickupPre(ItemEntityPickupEvent.Pre event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        long gameTime = player.level().getGameTime();

        SNAPSHOTS.entrySet().removeIf(
                entry -> gameTime - entry.getValue().gameTime() > 40
        );

        Inventory inventory = player.getInventory();
        boolean[] emptySlots = new boolean[36];

        for (int i = 0; i < emptySlots.length; i++) {
            emptySlots[i] = inventory.getItem(i).isEmpty();
        }

        SNAPSHOTS.put(event.getItemEntity().getUUID(), new PickupSnapshot(emptySlots, gameTime));
    }

    @SubscribeEvent
    public static void onPickupPost(ItemEntityPickupEvent.Post event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        ItemEntity itemEntity = event.getItemEntity();
        PickupSnapshot snapshot = SNAPSHOTS.remove(itemEntity.getUUID());

        if (snapshot == null) return;

        Inventory inventory = player.getInventory();

        for (int i = 0; i < snapshot.emptySlots().length; i++) {
            if (snapshot.emptySlots()[i] && !inventory.getItem(i).isEmpty()) {
                RecentItemS2CPacket.send(player, i);
            }
        }
    }

    private record PickupSnapshot(boolean[] emptySlots, long gameTime) {}
}