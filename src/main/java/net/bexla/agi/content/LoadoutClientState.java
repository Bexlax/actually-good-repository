package net.bexla.agi.content;

public final class LoadoutClientState {
    private static int selected;

    private LoadoutClientState() {}

    public static int getSelected() {
        return selected;
    }

    public static void setSelected(int loadout) {
        if (loadout >= 0 && loadout < 3) {
            selected = loadout;
        }
    }
}