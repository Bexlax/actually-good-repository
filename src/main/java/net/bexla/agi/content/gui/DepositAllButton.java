package net.bexla.agi.content.gui;

import net.bexla.agi.AGIMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class DepositAllButton extends Button {
    public ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(AGIMod.MODID,
                    "textures/gui/chest/deposit_all.png");

    public ResourceLocation TEXTURE_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(AGIMod.MODID,
                    "textures/gui/chest/deposit_all_highlighted.png");

    public int TEXTURE_WIDTH = 12;
    public int TEXTURE_HEIGHT = 16;

    public DepositAllButton(int x, int y, OnPress onPress) {
        super(x, y, 20, 20,
                Component.translatable("gui.agi.deposit_all"),
                onPress, DEFAULT_NARRATION);

        this.setTooltip(Tooltip.create(
                Component.translatable("gui.agi.deposit_all")
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