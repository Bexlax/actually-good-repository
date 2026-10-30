package net.bexla.agi.mixin;

import net.bexla.agi.content.FavoriteItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "drop(Z)Z", at = @At("HEAD"), cancellable = true)
    private void agi$suppressFavoriteDrop(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        ItemStack mainHandStack = ((ServerPlayer)(Object)this).getInventory().getSelected();

        if (!FavoriteItems.isFavorite(mainHandStack)) return;

        cir.setReturnValue(false);
    }
}