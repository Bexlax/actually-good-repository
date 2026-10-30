package net.bexla.agi.content;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Collection;

public final class CuriosLoadouts {
    private static final String DATA_KEY = "curios_loadouts";
    private static final int LOADOUT_COUNT = 3;

    private CuriosLoadouts() {}

    private static CompoundTag getData(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();

        if (!persistent.contains(DATA_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag data = new CompoundTag();
            ListTag loadouts = new ListTag();

            for (int i = 0; i < LOADOUT_COUNT; i++) {
                loadouts.add(new CompoundTag());
            }

            data.put("loadouts", loadouts);
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

        saveCurrentCurios(player, data, getSelected(player));
        restoreLoadout(player, data, targetLoadout);

        data.putInt("selected", targetLoadout);
        player.getPersistentData().put(DATA_KEY, data);
    }

    private static void saveCurrentCurios(ServerPlayer player, CompoundTag data, int loadoutIndex) {
        ListTag loadouts = data.getList("loadouts", Tag.TAG_COMPOUND);
        while (loadouts.size() <= loadoutIndex) {
            loadouts.add(new CompoundTag());
        }

        CompoundTag saved = new CompoundTag();

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            for (String identifier : handler.getCurios().keySet()) {
                handler.getStacksHandler(identifier).ifPresent(stacksHandler -> {
                    ListTag slotData = new ListTag();

                    for (int i = 0; i < stacksHandler.getStacks().getSlots(); i++) {
                        ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                        if (stack.isEmpty()) {
                            slotData.add(new CompoundTag());
                        } else {
                            slotData.add(stack.save(player.registryAccess()));
                        }
                    }

                    saved.put(identifier, slotData);
                });
            }
        });

        loadouts.set(loadoutIndex, saved);
        data.put("loadouts", loadouts);
    }

    private static void restoreLoadout(ServerPlayer player, CompoundTag data, int loadoutIndex) {
        ListTag loadouts = data.getList("loadouts", Tag.TAG_COMPOUND);
        if (loadoutIndex >= loadouts.size()) return;

        CompoundTag savedLoadout = loadouts.getCompound(loadoutIndex);

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            for (String identifier : handler.getCurios().keySet()) {
                handler.getStacksHandler(identifier).ifPresent(stacksHandler -> {
                    ListTag savedSlots = savedLoadout.getList(identifier, Tag.TAG_COMPOUND);

                    for (int i = 0; i < stacksHandler.getStacks().getSlots(); i++) {
                        ItemStack newStack = ItemStack.EMPTY;

                        if (i < savedSlots.size()) {
                            CompoundTag tag = savedSlots.getCompound(i);
                            if (!tag.isEmpty()) {
                                newStack = ItemStack.parse(player.registryAccess(), tag).orElse(ItemStack.EMPTY);
                            }
                        }

                        stacksHandler.getStacks().setStackInSlot(i, newStack);
                    }
                });
            }
        });
    }

    public static void dropStoredLoadouts(ServerPlayer player, Collection<ItemEntity> drops) {
        CompoundTag data = getData(player);
        ListTag loadouts = data.getList("loadouts", Tag.TAG_COMPOUND);

        int selected = data.getInt("selected");

        for (int loadout = 0; loadout < loadouts.size(); loadout++) {
            if (loadout == selected) continue;

            CompoundTag savedLoadout = loadouts.getCompound(loadout);

            for (String identifier : savedLoadout.getAllKeys()) {
                ListTag slots = savedLoadout.getList(identifier, Tag.TAG_COMPOUND);

                for (int slot = 0; slot < slots.size(); slot++) {
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

                savedLoadout.put(identifier, slots);
            }

            loadouts.set(loadout, savedLoadout);
        }

        data.put("loadouts", loadouts);
        player.getPersistentData().put(DATA_KEY, data);
    }
}