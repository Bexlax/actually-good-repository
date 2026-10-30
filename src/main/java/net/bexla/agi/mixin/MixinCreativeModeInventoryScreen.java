package net.bexla.agi.mixin;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class MixinCreativeModeInventoryScreen extends MixinAbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {

    protected MixinCreativeModeInventoryScreen(Component title) {
        super(title);
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void agi$handleCreativeFavoriteInteractions(Slot slot, int slotId, int mouseButton, ClickType clickType, CallbackInfo ci) {
        super.agi$handleFavoriteInteractions(slot, slotId, mouseButton, clickType, ci);
    }
}