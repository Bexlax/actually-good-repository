package net.bexla.agi.mixin;

import net.bexla.agi.AGIConfig;
import net.bexla.agi.content.FavoriteItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "split", at = @At("RETURN"))
    private void agi$modifySplitFavorite(int amount, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack original = (ItemStack) (Object) this;
        ItemStack split = cir.getReturnValue();

        if (!original.isEmpty() && !split.isEmpty()) {
            FavoriteItems.setFavorite(split, false);
        }
    }

    @Inject(method = "getMaxStackSize", at = @At("RETURN"), cancellable = true)
    private void agi$expandStackSize(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        int original = stack.getItem().getDefaultInstance().getOrDefault(DataComponents.MAX_STACK_SIZE, 1);

        if (stack.isDamageableItem() || original <= 1) return;

        if (original > 1)
            cir.setReturnValue((int)Math.round(original * AGIConfig.COMMON.stackSizeMultiplier.get()));
    }

    @ModifyArg(method = "lambda$static$3", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ExtraCodecs;intRange(II)Lcom/mojang/serialization/Codec;"), index = 1, remap = false)
    private static int agi$increaseCountLimit(int originalMax) {
        return Integer.MAX_VALUE;
    }
}