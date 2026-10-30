package net.bexla.agi.mixin;

import net.bexla.agi.content.FavoriteItems;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class MixinSlot {
    @Shadow
    @Final
    public Container container;

    @Inject(method = "getItem", at = @At("RETURN"))
    private void agi$checkInventoryType(CallbackInfoReturnable<ItemStack> cir) {
        if (!(container instanceof Inventory)) {
            FavoriteItems.setFavorite(cir.getReturnValue(), false);
        }
    }
}