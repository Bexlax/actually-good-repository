package net.bexla.agi.content;

import net.bexla.agi.AGIConfig;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.DiggerItem;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class InventorySorter {
    private InventorySorter() {}

    public static boolean sort(Player player) {
        if (player.level().isClientSide()) return false;

        AbstractContainerMenu menu = player.containerMenu;
        if (menu == player.inventoryMenu) return sortPlayerInventory(player.getInventory(), menu);

        return sortPrimaryContainer(player.getInventory(), menu);
    }

    private static boolean sortPlayerInventory(Inventory inventory, AbstractContainerMenu menu) {
        List<Slot> slots = new ArrayList<>();

        boolean includeHotbar = AGIConfig.CLIENT.sortIncludeHotbar.get();
        int start = includeHotbar ? 0 : 9;

        for (Slot slot : menu.slots) {
            if (slot.container != inventory) continue;

            int inventoryIndex = slot.getContainerSlot();
            if (inventoryIndex >= 9 && inventoryIndex < 36
                    || includeHotbar && inventoryIndex >= 0 && inventoryIndex < 9) slots.add(slot);
        }

        return sortSlots(slots);
    }

    private static boolean sortPrimaryContainer(Inventory playerInventory, AbstractContainerMenu menu) {
        List<Slot> slots = new ArrayList<>();

        net.minecraft.world.Container target = null;

        for (Slot slot : menu.slots) {
            if (slot.container != playerInventory) {
                target = slot.container;
                break;
            }
        }

        if (target == null) return false;

        for (Slot slot : menu.slots) {
            if (slot.container == target) slots.add(slot);
        }

        for (Slot slot : slots) {
            for (Slot source : slots) {
                ItemStack stack = source.getItem();

                if (!stack.isEmpty() && !slot.mayPlace(stack)) return false;
            }
        }

        return sortSlots(slots);
    }

    private static boolean sortSlots(List<Slot> slots) {
        if (slots.size() < 2) return false;

        boolean respectFavorites = AGIConfig.CLIENT.sortRespectFavorites.get();

        List<ItemStack> movable = new ArrayList<>();
        List<Slot> availableSlots = new ArrayList<>();

        for (Slot slot : slots) {
            ItemStack stack = slot.getItem();

            if (respectFavorites && !stack.isEmpty() && FavoriteItems.isFavorite(stack)) continue;

            availableSlots.add(slot);

            if (!stack.isEmpty()) movable.add(stack.copy());
        }

        if (availableSlots.isEmpty()) return false;

        List<ItemStack> consolidated = new ArrayList<>();

        for (ItemStack source : movable) {
            int remaining = source.getCount();

            for (ItemStack existing : consolidated) {
                if (!ItemStack.isSameItemSameComponents(existing, source)) continue;

                int capacity = existing.getMaxStackSize() - existing.getCount();

                if (capacity <= 0) continue;

                int moved = Math.min(remaining, capacity);
                existing.grow(moved);
                remaining -= moved;

                if (remaining == 0) break;
            }

            while (remaining > 0) {
                ItemStack part = source.copy();
                int count = Math.min(remaining, part.getMaxStackSize());
                part.setCount(count);
                consolidated.add(part);
                remaining -= count;
            }
        }

        consolidated.sort(
                Comparator.comparingInt((ItemStack stack) -> stack.getRarity().ordinal())
                        .reversed()
                        .thenComparingInt(InventorySorter::getCategory)
                        .thenComparing(stack -> stack.getItem().getDescriptionId())
        );

        if (consolidated.size() > availableSlots.size()) return false;

        for (Slot slot : availableSlots) {
            slot.set(ItemStack.EMPTY);
            slot.setChanged();
        }

        for (int i = 0; i < consolidated.size(); i++) {
            Slot slot = availableSlots.get(i);
            slot.set(consolidated.get(i));
            slot.setChanged();
        }

        return true;
    }

    private static int getCategory(ItemStack stack) {
        if (stack.is(ItemTags.WEAPON_ENCHANTABLE)) return 0;
        if (stack.is(ItemTags.MINING_ENCHANTABLE)) return 1;
        if (stack.is(ItemTags.ARMOR_ENCHANTABLE)) return 2;

        if (!stack.getAttributeModifiers().modifiers().isEmpty()) return 3;

        if (stack.is(Items.BOW)
                || stack.is(Items.CROSSBOW)
                || stack.is(Items.TRIDENT)
                || stack.is(Items.SHIELD)
                || stack.is(ItemTags.ARROWS)) return 3;

        if (stack.getFoodProperties(null) != null
                || stack.is(Tags.Items.POTIONS)
                || stack.is(Tags.Items.POTION_BOTTLE)) return 4;

        if (stack.is(ItemTags.ARROWS)) return 6;

        return 5;
    }
}