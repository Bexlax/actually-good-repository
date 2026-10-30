package net.bexla.agi.content;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Collection;

public final class ArmorLoadouts {
    private static final String DATA_KEY = "agi_armor_loadouts";
    private static final int LOADOUT_COUNT = 3;
    private static final int ARMOR_SLOTS = 4;

    private ArmorLoadouts() {}

    private static CompoundTag getData(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();

        if (!persistent.contains(DATA_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag data = new CompoundTag();
            ListTag loadouts = new ListTag();

            for (int loadout = 0; loadout < LOADOUT_COUNT; loadout++) {
                ListTag slots = new ListTag();

                for (int slot = 0; slot < ARMOR_SLOTS; slot++) {
                    slots.add(new CompoundTag());
                }

                loadouts.add(slots);
            }

            data.put("loadouts", loadouts);
            data.putIntArray("owners", new int[ARMOR_SLOTS]);
            data.putInt("selected", 0);

            persistent.put(DATA_KEY, data);
        }

        return persistent.getCompound(DATA_KEY);
    }

    public static int getSelected(ServerPlayer player) {
        return getData(player).getInt("selected");
    }

    public static void switchTo(ServerPlayer player, int targetLoadout) {
        if (targetLoadout < 0 || targetLoadout >= LOADOUT_COUNT) return;

        CompoundTag data = getData(player);
        Inventory inventory = player.getInventory();

        int[] owners = data.getIntArray("owners");
        if (owners.length != ARMOR_SLOTS) owners = new int[ARMOR_SLOTS];

        for (int slot = 0; slot < ARMOR_SLOTS; slot++) {
            if (owners[slot] == targetLoadout) continue;

            int inventorySlot = 36 + slot;

            ItemStack equipped = inventory.getItem(inventorySlot);
            ItemStack stored = getStack(player, data, targetLoadout, slot);

            if (FavoriteItems.isFavorite(equipped) || FavoriteItems.isFavorite(stored)) continue;

            int currentOwner = owners[slot];

            setStack(player, data, currentOwner, slot, equipped);

            inventory.setItem(inventorySlot, stored.copy());
            setStack(player, data, targetLoadout, slot, ItemStack.EMPTY);

            owners[slot] = targetLoadout;
        }

        data.putIntArray("owners", owners);
        data.putInt("selected", targetLoadout);
        player.getPersistentData().put(DATA_KEY, data);

        inventory.setChanged();
        player.containerMenu.broadcastChanges();
    }

    private static ItemStack getStack(ServerPlayer player, CompoundTag data, int loadout, int slot) {
        ListTag loadouts = data.getList("loadouts", Tag.TAG_LIST);

        if (loadout >= loadouts.size()) return ItemStack.EMPTY;
        ListTag slots = loadouts.getList(loadout);

        if (slot >= slots.size()) return ItemStack.EMPTY;
        CompoundTag stackTag = slots.getCompound(slot);

        return ItemStack.parse(player.registryAccess(), stackTag).orElse(ItemStack.EMPTY);
    }

    private static void setStack(ServerPlayer player, CompoundTag data, int loadout, int slot, ItemStack stack) {
        ListTag loadouts = data.getList("loadouts", Tag.TAG_LIST);

        while (loadouts.size() <= loadout) {
            ListTag slots = new ListTag();

            for (int i = 0; i < ARMOR_SLOTS; i++) {
                slots.add(new CompoundTag());
            }

            loadouts.add(slots);
        }

        ListTag slots = loadouts.getList(loadout);

        while (slots.size() <= slot) {
            slots.add(new CompoundTag());
        }

        slots.set(slot, stack.isEmpty() ? new CompoundTag() : stack.save(player.registryAccess()));
        loadouts.set(loadout, slots);
        data.put("loadouts", loadouts);
    }

    public static void dropStoredLoadouts(ServerPlayer player, Collection<ItemEntity> drops) {
        CompoundTag data = getData(player);
        ListTag loadouts = data.getList("loadouts", Tag.TAG_LIST);

        int[] owners = data.getIntArray("owners");

        if (owners.length != ARMOR_SLOTS) {
            owners = new int[ARMOR_SLOTS];
            Arrays.fill(owners, data.getInt("selected"));
        }

        for (int loadout = 0; loadout < loadouts.size(); loadout++) {
            ListTag slots = loadouts.getList(loadout);

            for (int slot = 0; slot < Math.min(slots.size(), ARMOR_SLOTS); slot++) {
                if (owners[slot] == loadout) continue;

                CompoundTag stackTag = slots.getCompound(slot);
                if (stackTag.isEmpty()) continue;

                ItemStack stack = ItemStack.parse(
                        player.registryAccess(), stackTag
                ).orElse(ItemStack.EMPTY);

                if (stack.isEmpty()) continue;

                drops.add(new ItemEntity(
                        player.level(),
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        stack
                ));

                slots.set(slot, new CompoundTag());
            }

            loadouts.set(loadout, slots);
        }

        data.put("loadouts", loadouts);
        player.getPersistentData().put(DATA_KEY, data);
    }
}