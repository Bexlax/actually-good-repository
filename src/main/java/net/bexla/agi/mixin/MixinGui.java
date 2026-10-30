package net.bexla.agi.mixin;

import com.mojang.blaze3d.platform.Window;
import net.bexla.agi.content.gui.FavoriteInGameHud;
import net.bexla.agi.content.gui.RenderUtil;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class MixinGui implements FavoriteInGameHud {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private int agi$fadeFavorite;

    @Unique
    private int agi$favoriteSlot;

    @Unique
    private float agi$fadeDivisor;

    @Override
    public void agi$setBookmarkFade(int durationMs) {
        Player player = minecraft.player;
        if (player == null) return;

        agi$fadeFavorite = durationMs / 100;
        agi$favoriteSlot = player.getInventory().selected;
        agi$fadeDivisor = durationMs / 200.0F;
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void agi$tickBookmarkFade(CallbackInfo ci) {
        if (agi$fadeFavorite > 0) {
            --agi$fadeFavorite;
        }
    }

    @Inject(method = "renderHotbar", at = @At("TAIL"))
    private void agi$renderBookmark(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Player player = minecraft.player;
        if (player == null || agi$fadeFavorite <= 0) return;

        int selectedSlot = player.getInventory().selected;
        if (selectedSlot != agi$favoriteSlot) return;

        Window window = minecraft.getWindow();
        int screenWidth = window.getGuiScaledWidth();
        int screenHeight = window.getGuiScaledHeight();

        int x = (screenWidth / 2) - 88 + selectedSlot * 20;
        int y = screenHeight - 19;

        RenderUtil.renderFavorite(graphics, x, y, agi$fadeFavorite / agi$fadeDivisor);
    }
}