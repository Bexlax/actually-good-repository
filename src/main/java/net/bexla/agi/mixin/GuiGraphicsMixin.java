package net.bexla.agi.mixin;

import net.bexla.agi.AGIConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.bexla.agi.AGIConfig.StackCountDisplayMode;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {

    @Shadow
    public abstract int drawString(Font font, String text, int x, int y, int color, boolean dropShadow);

    @Redirect(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"))
    private int agi$scaleStackCount(GuiGraphics graphics, Font font, String text, int x, int y, int color, boolean dropShadow) {
        StackCountDisplayMode mode = AGIConfig.CLIENT.stackDisplayMode.get();

        if (mode == StackCountDisplayMode.VANILLA) {
            return this.drawString(font, text, x, y, color, dropShadow);
        }

        String displayText = text;

        if (mode == StackCountDisplayMode.ABBREVIATED || mode == StackCountDisplayMode.SMALL_TEXT_ABBREVIATED) {
            displayText = agi$abbreviateCount(text);
        }

        int adjustedX = x + font.width(text) - font.width(displayText);

        boolean smallText = mode == StackCountDisplayMode.SMALL_TEXT_LONG || mode == StackCountDisplayMode.SMALL_TEXT_ABBREVIATED;

        if (!smallText || displayText.length() <= 2) {
            return this.drawString(
                    font, displayText, adjustedX, y, color, dropShadow
            );
        }

        float scale = Math.max(0.4F, 1.1F - displayText.length() * 0.1F);
        var pose = graphics.pose();

        float right = adjustedX + font.width(displayText);

        pose.pushPose();
        pose.translate(
                right * (1.0F - scale),
                y * (1.0F - scale),
                0
        );
        pose.scale(scale, scale, 1.0F);

        int result = this.drawString(
                font, displayText, adjustedX, y, color, dropShadow
        );

        pose.popPose();

        return result;
    }

    @Unique
    private static String agi$abbreviateCount(String text) {
        if (!text.matches("\\d+")) {
            return text;
        }

        long count;

        try {
            count = Long.parseLong(text);
        } catch (NumberFormatException ignored) {
            return text;
        }

        if (count < 1_000) {
            return text;
        }

        if (count < 1_000_000) {
            return agi$formatCompact(count / 1_000.0) + "K";
        }

        if (count < 1_000_000_000) {
            return agi$formatCompact(count / 1_000_000.0) + "M";
        }

        return agi$formatCompact(count / 1_000_000_000.0) + "B";
    }

    @Unique
    private static String agi$formatCompact(double value) {
        if (value >= 100 || value == Math.floor(value)) {
            return Long.toString((long) value);
        }

        return String.format(java.util.Locale.ROOT, "%.1f", value)
                .replaceAll("\\.?0+$", "");
    }
}