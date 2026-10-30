package net.bexla.agi.content;

import net.bexla.agi.AGIConfig;
import net.bexla.agi.AGIMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.*;
import java.util.function.Predicate;

public final class ChestTransfer {
    private static final int RADIUS = 8;

    private ChestTransfer() {}

    public enum TransferMode {
        SIMILAR_STACK,
        QUICK_STACK
    }

    public static void similarStack(ServerPlayer player) {
        transfer(player, TransferMode.SIMILAR_STACK);
    }

    public static void quickStack(ServerPlayer player) {
        transfer(player, TransferMode.QUICK_STACK);
    }

    public static void depositAll(ServerPlayer player) {
        Container chest = getOpenChest(player);
        if (chest == null) return;

        Inventory inventory = player.getInventory();

        boolean respectFavorites = AGIConfig.CLIENT.depositAllRespectFavorites.get();
        boolean includeHotbar = AGIConfig.CLIENT.depositAllIncludeHotbar.get();
        boolean includeOffhand = AGIConfig.CLIENT.depositAllIncludeOffhand.get();

        List<Integer> slots = new ArrayList<>();

        for (int i = 9; i < 36; i++) {
            slots.add(i);
        }

        if (includeHotbar) {
            for (int i = 0; i < 9; i++) {
                slots.add(i);
            }
        }

        if (includeOffhand) {
            slots.add(Inventory.SLOT_OFFHAND);
        }

        int totalMoved = 0;

        for (int slot : slots) {
            ItemStack source = inventory.getItem(slot);

            if (source.isEmpty()
                    || (respectFavorites && FavoriteItems.isFavorite(source))) {
                continue;
            }

            int originalCount = source.getCount();

            ItemStack remaining = insertInto(
                    respectFavorites,
                    chest,
                    source.copy(),
                    true,
                    0,
                    chest.getContainerSize()
            );

            int moved = originalCount - remaining.getCount();
            if (moved <= 0) continue;

            inventory.setItem(slot, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
            totalMoved += moved;
        }

        if (totalMoved > 0) {
            inventory.setChanged();
            chest.setChanged();
        }

    }

    public static void lootAll(ServerPlayer player) {
        Container chest = getOpenChest(player);
        if (chest == null) return;

        Inventory inventory = player.getInventory();
        int totalMoved = 0;

        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack source = chest.getItem(i);

            if (source.isEmpty()) continue;

            int originalCount = source.getCount();
            ItemStack remaining = insertInto(true, inventory, source.copy(), true, 9, 36);

            int moved = originalCount - remaining.getCount();
            if (moved <= 0) continue;

            chest.setItem(i, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
            totalMoved += moved;
        }

        if (totalMoved > 0) {
            inventory.setChanged();
            chest.setChanged();
        }

    }

    public static int restock(ServerPlayer player) {
        Container chest = getOpenChest(player);
        if (chest == null) return 0;

        Inventory inventory = player.getInventory();

        boolean respectFavorites = AGIConfig.CLIENT.restockRespectFavorites.get();
        boolean includeHotbar = AGIConfig.CLIENT.restockIncludeHotbar.get();
        boolean includeOffhand = AGIConfig.CLIENT.restockIncludeOffhand.get();

        List<Integer> slots = new ArrayList<>();

        for (int i = 9; i < 36; i++) {
            slots.add(i);
        }

        if (includeHotbar) {
            for (int i = 0; i < 9; i++) {
                slots.add(i);
            }
        }

        if (includeOffhand) {
            slots.add(Inventory.SLOT_OFFHAND);
        }

        int totalMoved = 0;

        for (int inventorySlot : slots) {
            ItemStack target = inventory.getItem(inventorySlot);

            if (target.isEmpty()
                    || (respectFavorites && FavoriteItems.isFavorite(target))) {
                continue;
            }

            int maxSize = Math.min(
                    target.getMaxStackSize(),
                    inventory.getMaxStackSize()
            );

            int capacity = maxSize - target.getCount();
            if (capacity <= 0) continue;

            ItemStack updatedTarget = target.copy();

            for (int chestSlot = 0;
                 chestSlot < chest.getContainerSize() && capacity > 0;
                 chestSlot++) {

                ItemStack source = chest.getItem(chestSlot);

                if (source.isEmpty() || !areEquals(target, source)) continue;

                int moved = Math.min(capacity, source.getCount());

                updatedTarget.grow(moved);
                capacity -= moved;

                ItemStack remaining = source.copy();
                remaining.shrink(moved);

                chest.setItem(
                        chestSlot,
                        remaining.isEmpty() ? ItemStack.EMPTY : remaining
                );

                totalMoved += moved;
            }

            if (!matches(target, updatedTarget)) {
                inventory.setItem(inventorySlot, updatedTarget);
            }
        }

        if (totalMoved > 0) {
            inventory.setChanged();
            chest.setChanged();
        }

        return totalMoved;
    }

    private static Container getOpenChest(ServerPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;

        if (!menu.stillValid(player)
                || !(menu instanceof ChestMenu || menu instanceof ShulkerBoxMenu)) return null;

        Inventory inventory = player.getInventory();

        for (var slot : menu.slots) {
            if (slot.container != inventory) return slot.container;
        }

        return null;
    }

    private static ItemStack insertInto(boolean skipFavorites, Container destination, ItemStack remaining, boolean allowEmptySlots, int startSlot, int endSlot) {
        if (remaining.isEmpty() || (skipFavorites && FavoriteItems.isFavorite(remaining))) return remaining;

        for (int i = startSlot; i < endSlot; i++) {
            ItemStack target = destination.getItem(i);

            if (target.isEmpty()
                    || (skipFavorites && FavoriteItems.isFavorite(target))
                    || !areEquals(target, remaining)
                    || !destination.canPlaceItem(i, remaining)) continue;

            int maxSize = Math.min(
                    target.getMaxStackSize(),
                    destination.getMaxStackSize()
            );

            int capacity = maxSize - target.getCount();
            if (capacity <= 0) continue;

            int moved = Math.min(capacity, remaining.getCount());

            ItemStack updated = target.copy();
            updated.grow(moved);
            remaining.shrink(moved);

            destination.setItem(i, updated);
            destination.setChanged();

            if (remaining.isEmpty()) return ItemStack.EMPTY;
        }

        if (!allowEmptySlots) return remaining;

        for (int i = startSlot; i < endSlot; i++) {
            if (!destination.getItem(i).isEmpty()
                    || !destination.canPlaceItem(i, remaining)) continue;

            int maxSize = Math.min(
                    remaining.getMaxStackSize(),
                    destination.getMaxStackSize()
            );

            int moved = Math.min(maxSize, remaining.getCount());

            ItemStack inserted = remaining.copy();
            inserted.setCount(moved);
            remaining.shrink(moved);

            destination.setItem(i, inserted);
            destination.setChanged();

            if (remaining.isEmpty()) return ItemStack.EMPTY;
        }

        return remaining;
    }

    private static void transfer(ServerPlayer player, TransferMode mode) {
        ServerLevel level = player.serverLevel();
        Inventory inventory = player.getInventory();
        boolean quickStack = mode == TransferMode.QUICK_STACK;

        int radius = quickStack? AGIConfig.COMMON.quickStackSearchRadius.get()
                : AGIConfig.COMMON.similarStackSearchRadius.get();
        boolean includeHotbar = quickStack? AGIConfig.CLIENT.quickStackIncludeHotbar.get()
                : AGIConfig.CLIENT.similarStackIncludeHotbar.get();
        boolean includeOffhand = quickStack? AGIConfig.CLIENT.quickStackIncludeOffhand.get()
                : AGIConfig.CLIENT.similarStackIncludeOffhand.get();
        boolean respectFavorites = quickStack? AGIConfig.CLIENT.quickStackRespectFavorites.get()
                : AGIConfig.CLIENT.similarStackRespectFavorites.get();

        List<NearbyContainer> containers = findContainers(level, player, radius);
        List<Integer> sourceSlots = new ArrayList<>();

        for (int slot = 9; slot < 36; slot++) {
            sourceSlots.add(slot);
        }

        if (includeHotbar) {
            for (int slot = 0; slot < 9; slot++) {
                sourceSlots.add(slot);
            }
        }

        if (includeOffhand)
            sourceSlots.add(Inventory.SLOT_OFFHAND);

        int totalMoved = 0;

        for (int inventorySlot : sourceSlots) {
            ItemStack source = inventory.getItem(inventorySlot);

            if (source.isEmpty() || (respectFavorites && FavoriteItems.isFavorite(source))) continue;

            int originalCount = source.getCount();
            ItemStack remaining = source.copy();

            boolean foundMatchingStack = false;

            for (NearbyContainer nearby : containers) {
                Container container = nearby.container();

                for (int slot = 0; slot < container.getContainerSize(); slot++) {
                    if (remaining.isEmpty()) break;

                    ItemStack target = container.getItem(slot);


                    if (target.isEmpty()
                            || (respectFavorites && FavoriteItems.isFavorite(target))
                            || !areEquals(target, remaining)
                            || !container.canPlaceItem(slot, remaining)) {
                        continue;
                    }

                    foundMatchingStack = true;

                    int maxSize = Math.min(
                            remaining.getMaxStackSize(),
                            Math.min(target.getMaxStackSize(), container.getMaxStackSize())
                    );

                    int capacity = maxSize - target.getCount();
                    if (capacity <= 0) continue;

                    int moved = Math.min(capacity, remaining.getCount());

                    ItemStack updatedTarget = target.copy();
                    updatedTarget.grow(moved);
                    remaining.shrink(moved);

                    container.setItem(slot, updatedTarget);
                    container.setChanged();
                }

                if (remaining.isEmpty()) break;
            }

            if (!remaining.isEmpty() && (quickStack || foundMatchingStack)) {
                for (NearbyContainer nearby : containers) {
                    Container container = nearby.container();

                    for (int slot = 0; slot < container.getContainerSize(); slot++) {
                        if (remaining.isEmpty()) break;

                        if (!container.getItem(slot).isEmpty()
                                || !container.canPlaceItem(slot, remaining)) {
                            continue;
                        }

                        int maxSize = Math.min(
                                remaining.getMaxStackSize(),
                                container.getMaxStackSize()
                        );

                        int moved = Math.min(maxSize, remaining.getCount());

                        ItemStack inserted = remaining.copy();
                        inserted.setCount(moved);
                        remaining.shrink(moved);

                        container.setItem(slot, inserted);
                        container.setChanged();
                    }

                    if (remaining.isEmpty()) break;
                }
            }

            int moved = originalCount - remaining.getCount();

            if (moved > 0) {
                inventory.setItem(
                        inventorySlot,
                        remaining.isEmpty() ? ItemStack.EMPTY : remaining
                );

                totalMoved += moved;
            }
        }

        if (totalMoved > 0) {
            inventory.setChanged();
        }
    }

    public static boolean areEquals(ItemStack first, ItemStack second) {
        if (!first.is(second.getItem())) return false;
        if (first.isEmpty() && second.isEmpty()) return true;
        
        Predicate<DataComponentType<?>> favoritePredicate = type -> type == AGIMod.FAVORITE.get();

        DataComponentPatch firstComponents = first.getComponentsPatch().forget(favoritePredicate);
        DataComponentPatch secondComponents = second.getComponentsPatch().forget(favoritePredicate);
        return Objects.equals(firstComponents, secondComponents);
    }

    public static boolean matches(ItemStack p_41729_, ItemStack p_41730_) {
        if (p_41729_ == p_41730_) {
            return true;
        } else {
            return p_41729_.getCount() == p_41730_.getCount() && areEquals(p_41729_, p_41730_);
        }
    }

    private static List<NearbyContainer> findContainers(ServerLevel level, ServerPlayer player, int radius) {
        double radiusSquared = (double) radius * radius;

        List<NearbyContainer> containers = new ArrayList<>();
        Set<BlockPos> handledPositions = new HashSet<>();

        BlockPos center = player.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {

            if (pos.distToCenterSqr(player.position()) > radiusSquared || !level.hasChunkAt(pos)) continue;

            BlockPos immutablePos = pos.immutable();

            if (handledPositions.contains(immutablePos)) continue;

            BlockState state = level.getBlockState(pos);

            boolean storageBlock = state.getBlock() instanceof AbstractChestBlock<?>
                    || state.getBlock() instanceof BarrelBlock
                    || state.getBlock() instanceof ShulkerBoxBlock;

            if (!storageBlock) continue;

            Container container;

            if (state.getBlock() instanceof ChestBlock chestBlock) {
                container = ChestBlock.getContainer(chestBlock, state, level, pos, true);

                if (state.hasProperty(ChestBlock.TYPE) && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                    Direction connectedDirection = ChestBlock.getConnectedDirection(state);

                    handledPositions.add(immutablePos.relative(connectedDirection));
                }
            } else {
                BlockEntity blockEntity = level.getBlockEntity(pos);

                if (!(blockEntity instanceof Container blockContainer)) continue;

                container = blockContainer;
            }

            if (container == null) continue;

            handledPositions.add(immutablePos);
            containers.add(new NearbyContainer(
                    container,
                    pos.distToCenterSqr(player.position())
            ));
        }

        containers.sort(Comparator.comparingDouble(NearbyContainer::distance));
        return containers;
    }

    private record NearbyContainer(Container container, double distance) {}
}