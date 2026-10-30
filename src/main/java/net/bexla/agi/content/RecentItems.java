package net.bexla.agi.content;

import java.util.HashSet;
import java.util.Set;

public final class RecentItems {
    private static final Set<Integer> RECENT_SLOTS = new HashSet<>();

    private RecentItems() {}

    public static void mark(int slot) {
        if (slot >= 0 && slot < 36)
            RECENT_SLOTS.add(slot);
    }

    public static boolean isRecent(int slot) {
        return RECENT_SLOTS.contains(slot);
    }

    public static void clear(int slot) {
        RECENT_SLOTS.remove(slot);
    }

    public static void clearAll() {
        RECENT_SLOTS.clear();
    }
}