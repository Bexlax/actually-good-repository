package net.bexla.agi.content.gui;

import net.bexla.agi.AGIMod;
import net.bexla.agi.content.LoadoutClientState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class LoadoutButton extends Button {
    private static final ResourceLocation[] TEXTURES_HIGHLIGHTED = {
            texture("loadout_0_highlighted.png"),
            texture("loadout_1_highlighted.png"),
            texture("loadout_2_highlighted.png")
    };

    private static final ResourceLocation[] TEXTURES_SELECTED = {
            texture("loadout_0_selected.png"),
            texture("loadout_1_selected.png"),
            texture("loadout_2_selected.png")
    };

    private static final ResourceLocation[] TEXTURES_SELECTED_HIGHLIGHTED = {
            texture("loadout_0_selected_highlighted.png"),
            texture("loadout_1_selected_highlighted.png"),
            texture("loadout_2_selected_highlighted.png")
    };

    private static final int TEXTURE_WIDTH = 16;
    private static final int TEXTURE_HEIGHT = 17;

    private final int loadout;

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(
                AGIMod.MODID,
                "textures/gui/inventory/loadout/" + name
        );
    }

    public LoadoutButton(int x, int y, int loadout, OnPress onPress) {
        super(x, y, 20, 20,
                Component.translatable("gui.agi.loadout", new String[]{"I", "II", "III"}[loadout]),
                onPress,
                DEFAULT_NARRATION);
        this.loadout = loadout;
        this.setTooltip(Tooltip.create(
                Component.translatable("gui.agi.loadout", new String[]{"I", "II", "III"}[loadout])
        ));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean selected = LoadoutClientState.getSelected() == loadout;
        boolean hovered = this.isHovered();

        ResourceLocation texture;

        if (selected) {
            texture = hovered
                    ? TEXTURES_SELECTED_HIGHLIGHTED[loadout]
                    : TEXTURES_SELECTED[loadout];
        } else {
            texture = hovered
                    ? TEXTURES_HIGHLIGHTED[loadout]
                    : texture("loadout_inactive.png");
        }

        int textureX = this.getX() + (this.width - TEXTURE_WIDTH) / 2;
        int textureY = this.getY() + (this.height - TEXTURE_HEIGHT) / 2;

        graphics.blit(
                texture,
                textureX,
                textureY,
                0, 0,
                TEXTURE_WIDTH, TEXTURE_HEIGHT,
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );
    }
}