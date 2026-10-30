package net.bexla.agi;

import com.mojang.blaze3d.platform.InputConstants;
import net.bexla.agi.content.FavoriteItems;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@EventBusSubscriber(modid = AGIMod.MODID, value = Dist.CLIENT)
public final class AGIClientEvents {

    public static final KeyMapping FAVORITE_KEY_MAPPING = new KeyMapping(
            "key.agi.favorite_item",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            "key.categories.inventory"
    );

    public static final KeyMapping HIDE_BUTTONS_MAPPING = new KeyMapping(
            "key.agi.hide_buttons",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.inventory"
    );

    private AGIClientEvents() {}

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(FAVORITE_KEY_MAPPING);
    }

    private static final MutableComponent MARKED_FAVORITE = Component.translatable("item.agi.marked_favorite");

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();

        if(FavoriteItems.isFavorite(stack)) {
            tooltip.add(1, MARKED_FAVORITE);
        }
    }
}