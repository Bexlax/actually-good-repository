package net.bexla.agi.content.gui;

import net.bexla.agi.AGIMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class QuickStackButton extends Button {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(AGIMod.MODID,
                    "textures/gui/inventory/quick_stack.png");

    private static final ResourceLocation TEXTURE_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(AGIMod.MODID,
                    "textures/gui/inventory/quick_stack_highlighted.png");

    private static final int TEXTURE_WIDTH = 17;
    private static final int TEXTURE_HEIGHT = 16;

    public QuickStackButton(int x, int y, OnPress onPress) {
        super(x, y, 20, 20,
                Component.translatable("gui.agi.quick_stack"),
                onPress, DEFAULT_NARRATION);

        this.setTooltip(Tooltip.create(
                Component.translatable("gui.agi.quick_stack")
        ));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int textureX = this.getX() + (this.width - TEXTURE_WIDTH) / 2;
        int textureY = this.getY() + (this.height - TEXTURE_HEIGHT) / 2;

        graphics.blit(
                this.isHovered()? TEXTURE_HIGHLIGHTED : TEXTURE,
                textureX,
                textureY,
                0,
                0,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }
}