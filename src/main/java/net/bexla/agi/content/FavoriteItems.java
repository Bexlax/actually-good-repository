package net.bexla.agi.content;

import net.bexla.agi.AGIMod;
import net.minecraft.world.item.ItemStack;

public final class FavoriteItems {
    private FavoriteItems() {}

    public static boolean isFavorite(ItemStack itemStack) {
        return !itemStack.isEmpty() && itemStack.getOrDefault(AGIMod.FAVORITE.get(), false);
    }

    public static void setFavorite(ItemStack itemStack, boolean status) {
        if (itemStack.isEmpty()) return;

        if (status) {
            itemStack.set(AGIMod.FAVORITE.get(), true);
        } else {
            itemStack.remove(AGIMod.FAVORITE.get());
        }
    }

    public static void setFavorite(ItemStack stack) {
        setFavorite(stack, !isFavorite(stack));
    }
}