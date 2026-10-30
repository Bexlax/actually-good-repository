package net.bexla.agi;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import org.apache.commons.lang3.tuple.Pair;


public class AGIConfig {
    public static final Common COMMON;
    public static final Client CLIENT;

    private static final ModConfigSpec COMMON_SPEC;
    private static final ModConfigSpec CLIENT_SPEC;

    public static class Common {

        public final ConfigValue<Double> stackSizeMultiplier;

        public final ConfigValue<Integer> quickStackSearchRadius;
        public final ConfigValue<Integer> similarStackSearchRadius;

        private Common(ModConfigSpec.Builder builder) {
            stackSizeMultiplier = builder
                    .translation("agi.config.stack_size_multiplier")
                    .comment("Multiplier that defines the max size of every Stack")
                    .defineInRange("stack_size_multiplier", 15D, 0D, 31250D);

//            builder.push("quick_stack");

            quickStackSearchRadius = builder
                    .translation("agi.config.quick_stack_search_radius")
                    .comment("Maximum distance in blocks for searching nearby containers")
                    .defineInRange("quick_stack_search_radius", 8, 1, 64);

//            builder.pop();
//
//            builder.push("similar_stack");

            similarStackSearchRadius = builder
                    .translation("agi.config.similar_stack_search_radius")
                    .comment("Maximum distance in blocks for searching nearby containers")
                    .defineInRange("similar_stack_search_radius", 8, 1, 64);

//            builder.pop();
        }
    }

    public enum Anchor {
        AUTO,
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    public enum ButtonLayout {
        ROW,
        COLUMN,
        GRID
    }

    public enum StackCountDisplayMode {
        SMALL_TEXT_LONG,
        ABBREVIATED,
        SMALL_TEXT_ABBREVIATED,
        VANILLA
    }

    public static class Client {
        public final ConfigValue<StackCountDisplayMode> stackDisplayMode;

        public final ConfigValue<Anchor> inventoryButtonsAnchor;
        public final ConfigValue<Anchor> loadoutButtonsAnchor;

        public final ConfigValue<ButtonLayout> inventoryButtonsLayout;

        public final ConfigValue<Integer> buttonSpacing;
        public final ConfigValue<Integer> buttonOffset;

        public final ConfigValue<Boolean> similarStackIncludeHotbar;
        public final ConfigValue<Boolean> similarStackIncludeOffhand;
        public final ConfigValue<Boolean> similarStackRespectFavorites;

        public final ConfigValue<Boolean> quickStackIncludeHotbar;
        public final ConfigValue<Boolean> quickStackIncludeOffhand;
        public final ConfigValue<Boolean> quickStackRespectFavorites;

        public final ConfigValue<Boolean> sortRespectFavorites;
        public final ConfigValue<Boolean> sortIncludeHotbar;
        
        public final ConfigValue<Boolean> depositAllRespectFavorites;
        public final ConfigValue<Boolean> depositAllIncludeHotbar;
        public final ConfigValue<Boolean> depositAllIncludeOffhand;
        
        public final ConfigValue<Boolean> restockRespectFavorites;
        public final ConfigValue<Boolean> restockIncludeHotbar;
        public final ConfigValue<Boolean> restockIncludeOffhand;

        private Client(ModConfigSpec.Builder builder) {
            stackDisplayMode = builder
                    .translation("agi.config.stack_display_mode")
                    .comment("Number display on item stacks")
                    .defineEnum("inventory_buttons_anchor", StackCountDisplayMode.SMALL_TEXT_LONG);

            builder.push("sorting");

            sortRespectFavorites = builder
                    .translation("agi.config.sort_respect_favorites")
                    .comment("Keep favorite items in their current slots when sorting")
                    .define("sort_respect_favorites", true);

            sortIncludeHotbar = builder
                    .translation("agi.config.sort_include_hotbar")
                    .comment("Include hotbar slots when sorting")
                    .define("sort_include_hotbar", false);

            builder.pop();

            builder.push("chest_actions");

            depositAllRespectFavorites = builder
                    .translation("agi.config.deposit_all_respect_favorites")
                    .comment("Keep favorite items in their current slots when depositing all items from your inventory into a chest")
                    .define("deposit_all_respect_favorites", true);

            depositAllIncludeHotbar = builder
                    .translation("agi.config.deposit_all_include_hotbar")
                    .comment("Include hotbar slots when depositing all items from your inventory into a chest")
                    .define("deposit_all_include_hotbar", false);

            depositAllIncludeOffhand = builder
                    .translation("agi.config.deposit_all_include_offhand")
                    .comment("Include hotbar slots when depositing all items from your inventory into a chest")
                    .define("deposit_all_include_offhand", false);

            restockRespectFavorites = builder
                    .translation("agi.config.restock_respect_favorites")
                    .comment("Keep favorite items in their current slots when restocking all items from a chest into your inventory")
                    .define("restock_respect_favorites", true);

            restockIncludeHotbar = builder
                    .translation("agi.config.restock_include_hotbar")
                    .comment("Include hotbar slots when restocking all items from a chest into your inventory")
                    .define("restock_include_hotbar", false);

            restockIncludeOffhand = builder
                    .translation("agi.config.restock_include_offhand")
                    .comment("Include hotbar slots when restocking all items from a chest into your inventory")
                    .define("restock_include_offhand", false);

            builder.pop();
            
            builder.push("quick_stack");

            quickStackIncludeHotbar = builder
                    .translation("agi.config.quick_stack_include_hotbar")
                    .comment("Allow Quick Stack to move items from the hotbar")
                    .define("quick_stack_include_hotbar", false);

            quickStackIncludeOffhand = builder
                    .translation("agi.config.quick_stack_include_offhand")
                    .comment("Allow Quick Stack to move the offhand item")
                    .define("quick_stack_include_offhand", false);

            quickStackRespectFavorites = builder
                    .translation("agi.config.quick_stack_respect_favorites")
                    .comment("Prevent Quick Stack from moving favorite items")
                    .define("quick_stack_respect_favorites", true);

            builder.pop();

            builder.push("similar_stack");

            similarStackIncludeHotbar = builder
                    .translation("agi.config.similar_stack_include_hotbar")
                    .comment("Allow Similar Stack to move items from the hotbar")
                    .define("similar_stack_include_hotbar", false);

            similarStackIncludeOffhand = builder
                    .translation("agi.config.similar_stack_include_offhand")
                    .comment("Allow Similar Stack to move the offhand item")
                    .define("similar_stack_include_offhand", false);

            similarStackRespectFavorites = builder
                    .translation("agi.config.similar_stack_respect_favorites")
                    .comment("Prevent Similar Stack from moving favorite items")
                    .define("similar_stack_respect_favorites", true);

            builder.pop();

            builder.push("button_positions");

            inventoryButtonsAnchor = builder
                    .translation("agi.config.inventory_buttons_anchor")
                    .comment("Anchor for Sort, Similar Stack, and Quick Stack", "WIP")
                    .defineEnum("inventory_buttons_anchor", Anchor.AUTO);

            loadoutButtonsAnchor = builder
                    .translation("agi.config.loadout_buttons_anchor")
                    .comment("Anchor for the armor loadout buttons", "WIP")
                    .defineEnum("loadout_buttons_anchor", Anchor.AUTO);

            builder.pop();

            builder.push("button_layout");

            buttonSpacing = builder
                    .translation("agi.config.button_spacing")
                    .comment("Spacing between adjacent buttons, in pixels")
                    .defineInRange("button_spacing", 2, 0, 16);

            buttonOffset = builder
                    .translation("agi.config.button_offset")
                    .comment("Distance between button groups and the inventory screen, in pixels")
                    .defineInRange("button_offset", 4, 0, 32);

            inventoryButtonsLayout = builder
                    .translation("agi.config.inventory_buttons_layout")
                    .comment("Layout for Sort, Similar Stack, and Quick Stack. Also affects Loot/Deposit all and Restock",
                            "WIP",
                            "ROW: horizontal row",
                            "COLUMN: vertical column",
                            "GRID: two columns, filled top to bottom")
                    .defineEnum("inventory_buttons_layout", ButtonLayout.COLUMN);
            
            builder.pop();
        }
    }

    static {
        final Pair<Common, ModConfigSpec> commonSpecPair =
                new ModConfigSpec.Builder().configure(Common::new);

        final Pair<Client, ModConfigSpec> clientSpecPair =
                new ModConfigSpec.Builder().configure(Client::new);

        COMMON_SPEC = commonSpecPair.getRight();
        CLIENT_SPEC = clientSpecPair.getRight();

        COMMON = commonSpecPair.getLeft();
        CLIENT = clientSpecPair.getLeft();
    }

    public static void register(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }
}
