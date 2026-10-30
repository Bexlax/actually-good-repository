package net.bexla.agi.content.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.bexla.agi.AGIMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderUtil {
    private static final ResourceLocation FAVORITE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AGIMod.MODID, "textures/gui/inventory/favorite.png");

    private static final ResourceLocation FAVORITE_BACK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AGIMod.MODID, "textures/gui/inventory/favorite_back.png");

    private static final ResourceLocation RECENT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AGIMod.MODID, "textures/gui/inventory/recent.png");

    private static final ResourceLocation RECENT_BACK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AGIMod.MODID, "textures/gui/inventory/recent_back.png");

    public static void renderFavoriteBack(GuiGraphics graphics, int x, int y, float alpha) {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        graphics.blit(
                FAVORITE_BACK_TEXTURE,
                x, y,
                0, 0,
                16, 16,
                16, 16
        );

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void renderFavorite(GuiGraphics graphics, int x, int y, float alpha) {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShaderColor(
                1.0F,
                1.0F,
                1.0F,
                alpha
        );

        graphics.blit(
                FAVORITE_TEXTURE,
                x, y,
                0, 0,
                16, 16,
                16, 16
        );

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void renderRecentItemBack(GuiGraphics graphics, int x, int y, float alpha) {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        graphics.blit(
                RECENT_BACK_TEXTURE,
                x, y,
                0, 0,
                16, 16,
                16, 16
        );

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void renderRecentItem(GuiGraphics graphics, int x, int y, float alpha) {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        graphics.blit(
                RECENT_TEXTURE,
                x, y,
                0, 0,
                16, 16,
                16, 16
        );

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}