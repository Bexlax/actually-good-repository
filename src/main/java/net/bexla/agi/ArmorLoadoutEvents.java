package net.bexla.agi;

import net.bexla.agi.content.ArmorLoadouts;
import net.bexla.agi.content.CuriosLoadouts;
import net.bexla.agi.packets.LoadoutStateS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = AGIMod.MODID)
public final class ArmorLoadoutEvents {
    private ArmorLoadoutEvents() {}

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LoadoutStateS2CPacket.send(player, ArmorLoadouts.getSelected(player));
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) return;

        ArmorLoadouts.dropStoredLoadouts(player, event.getDrops());
        if (ModList.get().isLoaded("curios"))
            CuriosLoadouts.dropStoredLoadouts(player, event.getDrops());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getOriginal() instanceof ServerPlayer original)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        CompoundTag persistent = original.getPersistentData();

        if (persistent.contains("agi_armor_loadouts", Tag.TAG_COMPOUND)) {
            player.getPersistentData().put("agi_armor_loadouts",
                    persistent.getCompound("agi_armor_loadouts").copy());
        }

        if (persistent.contains("curios_loadouts", Tag.TAG_COMPOUND)) {
            player.getPersistentData().put("curios_loadouts",
                    persistent.getCompound("curios_loadouts").copy());
        }
    }
}