package net.bexla.agi.mixin;

import net.bexla.agi.AGIConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BundleItem.class)
public class BundleItemMixin {
    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void agi$expandBundleCapacityTooltip(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
        BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (contents == null) return;

        int maxCapacity = (int) (Math.round(64 * AGIConfig.COMMON.stackSizeMultiplier.get()));
        int fullness = Mth.mulAndTruncate(contents.weight(), maxCapacity);

        tooltip.add(Component.translatable("item.minecraft.bundle.fullness", fullness, maxCapacity).withStyle(ChatFormatting.GRAY));
        ci.cancel();
    }
}
