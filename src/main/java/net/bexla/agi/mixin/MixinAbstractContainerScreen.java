package net.bexla.agi.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.bexla.agi.AGIConfig;
import net.bexla.agi.AGIMod;
import net.bexla.agi.AGIClientEvents;
import net.bexla.agi.content.FavoriteItems;
import net.bexla.agi.content.LoadoutClientState;
import net.bexla.agi.content.RecentItems;
import net.bexla.agi.content.gui.*;
import net.bexla.agi.packets.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreen<T extends AbstractContainerMenu> extends Screen {
    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Shadow
    protected int imageWidth;

    @Shadow
    protected int imageHeight;

    @Shadow
    @Final
    protected T menu;

    @Shadow
    @Final
    protected Set<Slot> quickCraftSlots;

    @Unique
    private Button agi$sortButton;

    @Unique
    private Button agi$similarStackButton;

    @Unique
    private Button agi$quickStackButton;

    @Unique
    private final LoadoutButton[] agi$loadoutButtons = new LoadoutButton[3];

    @Unique
    private DepositAllButton agi$depositAllButton;

    @Unique
    private LootAllButton agi$lootAllButton;

    @Unique
    private RestockButton agi$restockButton;

    @Unique
    private boolean agi$hideModButtons = false;

    @Unique
    private boolean agi$hideButtonsKeyWasDown = false;

    protected MixinAbstractContainerScreen(Component title) {
        super(title);
    }


    @Unique
    private int[] agi$getAnchorPosition(AGIConfig.Anchor anchor, int groupWidth, int groupHeight, boolean preferRight) {
        int offset = AGIConfig.CLIENT.buttonOffset.get();

        int left = leftPos - groupWidth - offset;
        int right = leftPos + imageWidth + offset;
        int top = topPos - groupHeight - offset;
        int bottom = topPos + imageHeight + offset;

        int centeredX = leftPos + (imageWidth - groupWidth) / 2;
        int centeredY = topPos + (imageHeight - groupHeight) / 2;

        return switch (anchor) {
            case LEFT -> new int[]{left, centeredY};
            case RIGHT -> new int[]{right, centeredY};
            case TOP -> new int[]{centeredX, top};
            case BOTTOM -> new int[]{centeredX, bottom};

            case TOP_LEFT -> new int[]{left, top};
            case TOP_RIGHT -> new int[]{right, top};
            case BOTTOM_LEFT -> new int[]{left, bottom};
            case BOTTOM_RIGHT -> new int[]{right, bottom};

            case AUTO -> {
                boolean canFitRight = right >= 0
                        && right + groupWidth <= this.width;

                boolean canFitLeft = left >= 0
                        && left + groupWidth <= this.width;

                if (preferRight && canFitRight) {
                    yield new int[]{right, topPos + offset};
                } else if (!preferRight && canFitLeft) {
                    yield new int[]{left, topPos + offset};
                } else if (canFitRight) {
                    yield new int[]{right, topPos + offset};
                } else if (canFitLeft) {
                    yield new int[]{left, topPos + offset};
                } else {
                    yield new int[]{
                            Math.max(0, Math.min(centeredX, this.width - groupWidth)),
                            Math.max(0, Math.min(topPos + offset, this.height - groupHeight))
                    };
                }
            }
        };
    }

    @Unique
    private void agi$positionActionButtons(Button[] buttons, int buttonWidth, int buttonHeight, int spacing, AGIConfig.ButtonLayout layout, AGIConfig.Anchor anchor) {
        int count = buttons.length;
        int columns = switch (layout) {
            case ROW -> count;
            case COLUMN -> 1;
            case GRID -> (count + 1) / 2;
        };
        int rows = (count + columns - 1) / columns;

        int groupWidth = columns * buttonWidth + (columns - 1) * spacing;
        int groupHeight = rows * buttonHeight + (rows - 1) * spacing;

        int[] position = agi$getAnchorPosition(
                anchor, groupWidth, groupHeight, true
        );

        for (int i = 0; i < count; i++) {
            int column;
            int row;

            switch (layout) {
                case ROW -> {
                    column = i;
                    row = 0;
                }
                case COLUMN -> {
                    column = 0;
                    row = i;
                }
                case GRID -> {
                    column = i % columns;
                    row = i / columns;
                }
                default -> throw new IllegalStateException(
                        "Unhandled button layout: " + layout
                );
            }

            buttons[i].setPosition(
                    position[0] + column * (buttonWidth + spacing),
                    position[1] + row * (buttonHeight + spacing)
            );
        }
    }

    @Unique
    private static void agi$setNativeCursorVisible(boolean visible) {
        long window = Minecraft.getInstance().getWindow().getWindow();

        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, visible ? GLFW.GLFW_CURSOR_NORMAL : GLFW.GLFW_CURSOR_HIDDEN);
    }

    @Unique
    private static final ResourceLocation agi$STAR_CURSOR = ResourceLocation.fromNamespaceAndPath(AGIMod.MODID,
                    "textures/gui/star_cursor.png");

    @Inject(method = "render", at = @At("RETURN"))
    private void agi$renderFavoriteCursor(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean shouldRender = minecraft.player != null && hoveredSlot != null && hoveredSlot.hasItem() && agi$isFavoriteModifierDown();

        agi$setNativeCursorVisible(!shouldRender);

        if (shouldRender) {
            RenderSystem.disableDepthTest();
            graphics.pose().pushPose();
            graphics.pose().translate(-1.74, -1.74, 100);

            graphics.blit(
                    agi$STAR_CURSOR, mouseX, mouseY,
                    0, 0,
                    10, 10,
                    10, 10);

            RenderSystem.enableDepthTest();
            graphics.pose().popPose();
        }
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void agi$restoreNativeCursor(CallbackInfo ci) {
        agi$setNativeCursorVisible(true);
    }

    @Unique
    private boolean agi$isRecipeBookOpen() {
        Minecraft minecraft = Minecraft.getInstance();

        return minecraft.screen instanceof InventoryScreen inventoryScreen
                && inventoryScreen.getRecipeBookComponent().isVisible();
    }

    @Unique
    private boolean agi$shouldShowAnchoredButtons(AGIConfig.Anchor anchor) {
        return !agi$hideModButtons && !(agi$isRecipeBookOpen() && anchor == AGIConfig.Anchor.LEFT);
    }

    @Unique
    private int agi$avoidButtonOverlap(Button[] actionButtons, int loadoutX, int loadoutY, int loadoutWidth, int loadoutHeight, int gap) {
        int actionLeft = Integer.MAX_VALUE;
        int actionRight = Integer.MIN_VALUE;
        int actionTop = Integer.MAX_VALUE;
        int actionBottom = Integer.MIN_VALUE;

        for (Button button : actionButtons) {
            if (button == null || !button.visible) continue;

            actionLeft = Math.min(actionLeft, button.getX());
            actionRight = Math.max(actionRight, button.getX() + button.getWidth());
            actionTop = Math.min(actionTop, button.getY());
            actionBottom = Math.max(actionBottom, button.getY() + button.getHeight());
        }

        if (actionLeft == Integer.MAX_VALUE) return loadoutY;

        boolean overlapsHorizontally = loadoutX < actionRight
                && loadoutX + loadoutWidth > actionLeft;

        boolean overlapsVertically = loadoutY < actionBottom
                && loadoutY + loadoutHeight > actionTop;

        if (overlapsHorizontally && overlapsVertically) return actionBottom + gap;

        return loadoutY;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void agi$repositionInventoryButtons(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();

        agi$updateHideButtonsToggle();
        agi$setModButtonsVisible(!agi$hideModButtons);

        if (minecraft.player == null
                || minecraft.screen instanceof CreativeModeInventoryScreen
                || agi$sortButton == null) return;

        int buttonWidth = 20;
        int buttonHeight = 20;
        int spacing = AGIConfig.CLIENT.buttonSpacing.get();

        Button[] actionButtons = getActionButtons();

        agi$positionActionButtons(
                actionButtons,
                buttonWidth,
                buttonHeight,
                spacing,
                AGIConfig.CLIENT.inventoryButtonsLayout.get(),
                AGIConfig.CLIENT.inventoryButtonsAnchor.get()
        );

        boolean showActionButtons = agi$shouldShowAnchoredButtons(AGIConfig.CLIENT.inventoryButtonsAnchor.get());

        for (Button button : actionButtons) {
            if (button != null) {
                button.visible = showActionButtons;
                button.active = showActionButtons;
            }
        }

        if (menu == minecraft.player.inventoryMenu) {
            int loadoutButtonSize = 20;
            int loadoutGap = 2;
            int loadoutGroupHeight = loadoutButtonSize * 3 + loadoutGap * 2;

            int[] loadoutPos = agi$getAnchorPosition(
                    AGIConfig.CLIENT.loadoutButtonsAnchor.get(),
                    loadoutButtonSize, loadoutGroupHeight, false);

            int loadoutX = loadoutPos[0];
            int loadoutY = loadoutPos[1];

            loadoutY = agi$avoidButtonOverlap(
                    actionButtons,
                    loadoutX,
                    loadoutY,
                    loadoutButtonSize,
                    loadoutGroupHeight,
                    2
            );

            boolean recipeBookOpen = minecraft.screen instanceof InventoryScreen inventoryScreen
                    && inventoryScreen.getRecipeBookComponent().isVisible();

            boolean showLoadoutButtons = !agi$hideModButtons
                    && !(recipeBookOpen && agi$shouldShowAnchoredButtons(AGIConfig.CLIENT.loadoutButtonsAnchor.get()));

            for (int i = 0; i < agi$loadoutButtons.length; i++) {
                LoadoutButton button = agi$loadoutButtons[i];

                if (button != null) {
                    button.setPosition(
                            loadoutX,
                            loadoutY + i * (loadoutButtonSize + loadoutGap)
                    );

                    button.visible = showLoadoutButtons;
                    button.active = showLoadoutButtons;
                }
            }
        }
    }

    private Button @NotNull [] getActionButtons() {
        boolean hasChestButtons = agi$depositAllButton != null
                && agi$lootAllButton != null
                && agi$restockButton != null;

        Button[] actionButtons;

        if (hasChestButtons) {
            actionButtons = new Button[] {
                    agi$sortButton,
                    agi$similarStackButton,
                    agi$quickStackButton,
                    agi$depositAllButton,
                    agi$lootAllButton,
                    agi$restockButton
            };
        } else {
            actionButtons = new Button[] {
                    agi$sortButton,
                    agi$similarStackButton,
                    agi$quickStackButton
            };
        }
        return actionButtons;
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    private void agi$addInventoryButtons(CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.screen instanceof CreativeModeInventoryScreen) return;

        int buttonWidth = 20;
        int buttonHeight = 20;
        int buttonGap = 2;
        int pairWidth = buttonWidth * 3 + buttonGap * 2;

        int[] inventoryPos = agi$getAnchorPosition(
                AGIConfig.CLIENT.inventoryButtonsAnchor.get(),
                pairWidth,
                buttonHeight,
                true
        );

        int x = inventoryPos[0];
        int y = inventoryPos[1];

        agi$sortButton = new SortInventoryButton(x, y,
                button -> SortInventoryC2SPacket.send(menu.containerId)
        );

        agi$similarStackButton = new SimilarStackButton(
                x + buttonWidth + buttonGap, y,
                button -> SimilarStackC2SPacket.send(menu.containerId)
        );

        agi$quickStackButton = new QuickStackButton(
                x + (buttonWidth + buttonGap) * 2, y,
                button -> QuickStackC2SPacket.send(menu.containerId)
        );

        this.addRenderableWidget(agi$sortButton);
        this.addRenderableWidget(agi$similarStackButton);
        this.addRenderableWidget(agi$quickStackButton);

        if (menu == minecraft.player.inventoryMenu) {
            int loadoutButtonSize = 20;
            int loadoutGap = 2;
            int totalHeight = loadoutButtonSize * 3 + loadoutGap * 2;

            int[] loadoutPos = agi$getAnchorPosition(
                    AGIConfig.CLIENT.loadoutButtonsAnchor.get(),
                    loadoutButtonSize,
                    totalHeight,
                    false
            );

            int loadoutX = loadoutPos[0];
            int loadoutY = loadoutPos[1];

            if (loadoutX < 0) {
                loadoutX = leftPos + 4;
            }

            for (int i = 0; i < 3; i++) {
                int loadout = i;

                agi$loadoutButtons[loadout] = new LoadoutButton(
                        loadoutX,
                        loadoutY + loadout * (loadoutButtonSize + loadoutGap),
                        loadout,
                        button -> {
                            LoadoutClientState.setSelected(loadout);
                            LoadoutSelectC2SPacket.send(loadout);
                        }
                );

                this.addRenderableWidget(agi$loadoutButtons[loadout]);
            }
        }

        if (menu instanceof ChestMenu || menu instanceof ShulkerBoxMenu) {
            agi$depositAllButton = new DepositAllButton(
                    0, 0,
                    button -> ChestActionC2SPacket.send(
                            menu.containerId,
                            ChestActionC2SPacket.Action.DEPOSIT_ALL
                    )
            );

            agi$lootAllButton = new LootAllButton(
                    0, 0,
                    button -> ChestActionC2SPacket.send(
                            menu.containerId,
                            ChestActionC2SPacket.Action.LOOT_ALL
                    )
            );

            agi$restockButton = new RestockButton(
                    0, 0,
                    button -> ChestActionC2SPacket.send(
                            menu.containerId,
                            ChestActionC2SPacket.Action.RESTOCK
                    )
            );

            this.addRenderableWidget(agi$depositAllButton);
            this.addRenderableWidget(agi$lootAllButton);
            this.addRenderableWidget(agi$restockButton);
        }
    }

    @Unique
    private void agi$toggleFavorite(Slot slot) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemStack stack = slot.getItem();
        boolean favorite = !FavoriteItems.isFavorite(stack);

        FavoriteItems.setFavorite(stack);

        SyncFavoriteItemsC2SPacket.send(
                menu.containerId,
                slot.index,
                favorite
        );

        minecraft.player.playSound(
                favorite ? SoundEvents.ARMADILLO_SCUTE_DROP : SoundEvents.HEAVY_CORE_HIT
        );
    }

    @Unique
    private void agi$setModButtonsVisible(boolean visible) {
        if (agi$sortButton != null) {
            agi$sortButton.visible = visible;
            agi$sortButton.active = visible;
        }

        if (agi$similarStackButton != null) {
            agi$similarStackButton.visible = visible;
            agi$similarStackButton.active = visible;
        }

        if (agi$quickStackButton != null) {
            agi$quickStackButton.visible = visible;
            agi$quickStackButton.active = visible;
        }

        for (LoadoutButton button : agi$loadoutButtons) {
            if (button != null) {
                button.visible = visible;
                button.active = visible;
            }
        }

        if (agi$depositAllButton != null) {
            agi$depositAllButton.visible = visible;
            agi$depositAllButton.active = visible;
        }

        if (agi$lootAllButton != null) {
            agi$lootAllButton.visible = visible;
            agi$lootAllButton.active = visible;
        }

        if (agi$restockButton != null) {
            agi$restockButton.visible = visible;
            agi$restockButton.active = visible;
        }
    }

    @Unique
    private boolean agi$isHideButtonsPressed() {
        Minecraft minecraft = Minecraft.getInstance();
        InputConstants.Key key = AGIClientEvents.HIDE_BUTTONS_MAPPING.getKey();

        if (key.getType() != InputConstants.Type.KEYSYM) {
            return false;
        }

        return InputConstants.isKeyDown(
                minecraft.getWindow().getWindow(),
                key.getValue()
        );
    }

    @Unique
    private void agi$updateHideButtonsToggle() {
        boolean keyDown = agi$isHideButtonsPressed();

        if (keyDown && !agi$hideButtonsKeyWasDown) {
            agi$hideModButtons = !agi$hideModButtons;
        }

        agi$hideButtonsKeyWasDown = keyDown;
    }

    @Unique
    private boolean agi$isFavoriteModifierDown() {
        Minecraft minecraft = Minecraft.getInstance();
        InputConstants.Key key = AGIClientEvents.FAVORITE_KEY_MAPPING.getKey();

        if (key.getType() != InputConstants.Type.KEYSYM) {
            return false;
        }

        return InputConstants.isKeyDown(
                minecraft.getWindow().getWindow(),
                key.getValue()
        );
    }

    @Shadow protected int leftPos;
    @Shadow protected int topPos;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void agi$handleFavoriteShortcut(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {

        if (button != 0 || !agi$isFavoriteModifierDown()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            AGIMod.LOGGER.error("player is null");
            return;
        }

        Slot target = null;

        for (Slot slot : menu.slots) {
            int x = leftPos + slot.x;
            int y = topPos + slot.y;

            if (mouseX >= x && mouseX < x + 16
                    && mouseY >= y && mouseY < y + 16) {
                target = slot;
                break;
            }
        }

        if (target == null) {
            AGIMod.LOGGER.info("no slot at mouse position");
            return;
        }

        if (!target.hasItem()) {
            AGIMod.LOGGER.info("target slot is empty, index={}", target.index);
            return;
        }

        if (target.container != minecraft.player.getInventory()) {
            AGIMod.LOGGER.info("target is not a player inventory slot");
            return;
        }

        if (!menu.getCarried().isEmpty()) {
            AGIMod.LOGGER.info("cursor is holding an item");
            return;
        }

        agi$toggleFavorite(target);

        cir.setReturnValue(true);
        cir.cancel();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void agi$clearRecentItemOnHover(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (minecraft.player == null || hoveredSlot == null) return;

        Slot slot = hoveredSlot;

        if (slot.container != minecraft.player.getInventory()) return;

        RecentItems.clear(slot.index);
    }

    @Inject(method = "renderSlot", at = @At("TAIL"))
    private void agi$renderRecentItem(GuiGraphics graphics, Slot slot, CallbackInfo ci) {
        if (minecraft.player == null
                || slot.container != minecraft.player.getInventory()
                || !RecentItems.isRecent(slot.index)
                || minecraft.screen instanceof CreativeModeInventoryScreen) return;

        int inventorySlot = slot.index;
        if (!RecentItems.isRecent(inventorySlot)) return;

        RenderUtil.renderRecentItem(graphics, slot.x, slot.y, 1.0F);
    }

    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void agi$renderRecentItemBack(GuiGraphics graphics, Slot slot, CallbackInfo ci) {
        if (minecraft.player == null
                || slot.container != minecraft.player.getInventory()
                || !RecentItems.isRecent(slot.index)
                || minecraft.screen instanceof CreativeModeInventoryScreen) return;

        RenderUtil.renderRecentItemBack(graphics, slot.x, slot.y, 0.9F);
    }

    @Inject(method = "renderSlot", at = @At("TAIL"))
    private void agi$renderFavorite(GuiGraphics graphics, Slot slot, CallbackInfo ci) {
        if (!FavoriteItems.isFavorite(slot.getItem())) return;

        RenderUtil.renderFavorite(graphics, slot.x, slot.y, 1.0F);
    }

    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void agi$renderFavoriteBack(GuiGraphics graphics, Slot slot, CallbackInfo ci) {
        if (!FavoriteItems.isFavorite(slot.getItem())) return;

        RenderUtil.renderFavoriteBack(graphics, slot.x, slot.y, 0.9F);
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    protected void agi$handleFavoriteInteractions(Slot slot, int slotId, int mouseButton, ClickType clickType, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemStack carried = menu.getCarried();
        boolean carriedFavorite = FavoriteItems.isFavorite(carried);

        if (slot == null) {
            if (slotId == -999
                    && clickType == ClickType.PICKUP
                    && carriedFavorite) {
                ci.cancel();
            }
            return;
        }

        ItemStack stack = slot.getItem();
        boolean slotFavorite = FavoriteItems.isFavorite(stack);
        boolean playerOwned = minecraft.player != null
                && slot.container == minecraft.player.getInventory();

        int depositCount = mouseButton != 0 ? 1 : carried.getCount();

        boolean canFullyMerge =
                !carried.isEmpty()
                        && !stack.isEmpty()
                        && ItemStack.isSameItemSameComponents(carried, stack)
                        && slot.getMaxStackSize(stack) >= stack.getCount() + depositCount
                        && carried.getCount() == depositCount;

        switch (clickType) {
            case PICKUP -> {
                if (playerOwned) {
                    if (carriedFavorite && canFullyMerge) {
                        FavoriteItems.setFavorite(stack, true);
                    }
                } else if (carriedFavorite) {
                    FavoriteItems.setFavorite(carried, false);
                }
            }

            case QUICK_MOVE -> {
                if (slotFavorite) {
                    ci.cancel();
                }
            }

            case SWAP -> {
                if (minecraft.player != null && mouseButton >= 0 && mouseButton < 9) {
                    ItemStack hotbarStack =
                            minecraft.player.getInventory().getItem(mouseButton);

                    if (FavoriteItems.isFavorite(hotbarStack) && !playerOwned) {
                        ci.cancel();
                    }
                }
            }

            case THROW -> {
                if (slotFavorite || carriedFavorite) {
                    ci.cancel();
                }
            }

            case QUICK_CRAFT -> {
                if (carriedFavorite && canFullyMerge && quickCraftSlots.size() == 1) {
                    FavoriteItems.setFavorite(stack);
                }
            }

            default -> {}
        }
    }
}