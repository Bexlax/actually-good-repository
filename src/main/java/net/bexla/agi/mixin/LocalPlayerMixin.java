package net.bexla.agi.mixin;

import net.bexla.agi.content.gui.FavoriteInGameHud;
import net.bexla.agi.content.FavoriteItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Inject(method = "drop(Z)Z", at = @At("HEAD"), cancellable = true)
    private void agi$suppressFavoriteDrop(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        ItemStack mainHandStack = ((LocalPlayer)(Object)this).getInventory().getSelected();

        if (!FavoriteItems.isFavorite(mainHandStack)) return;

        FavoriteInGameHud.setBookmarkFade(
                Minecraft.getInstance().gui,
                2500
        );

        cir.setReturnValue(false);
    }
}