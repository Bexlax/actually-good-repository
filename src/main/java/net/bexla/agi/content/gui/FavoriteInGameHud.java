package net.bexla.agi.content.gui;

import net.minecraft.client.gui.Gui;

public interface FavoriteInGameHud {
    void agi$setBookmarkFade(int durationMs);

    static void setBookmarkFade(Gui gui, int durationMs) {
        ((FavoriteInGameHud) gui).agi$setBookmarkFade(durationMs);
    }
}