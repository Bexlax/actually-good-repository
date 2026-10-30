package net.bexla.agi.mixin;

import net.bexla.agi.content.FavoriteItems;
import net.blay09.mods.trashslot.TrashHelper;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrashHelper.class)
public abstract class TrashHelperMixin {
    @Inject(method = "canDelete", at = @At("HEAD"), cancellable = true)
    private static void agi$protectFavorite(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (FavoriteItems.isFavorite(itemStack)) {
            cir.setReturnValue(false);
        }
    }
}