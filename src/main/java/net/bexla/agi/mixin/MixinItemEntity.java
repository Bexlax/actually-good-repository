package net.bexla.agi.mixin;

import net.bexla.agi.content.FavoriteItems;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity {

    @ModifyVariable(method = "setItem", at = @At("HEAD"), argsOnly = true)
    private ItemStack agi$removeFavoriteStatus(ItemStack stack) {
        FavoriteItems.setFavorite(stack, false);
        return stack;
    }
}