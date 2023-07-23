package com.aiyostudio.bingo.listen;

import com.aiyostudio.bingo.api.event.BingoQuestCompleteEvent;
import com.aiyostudio.bingo.api.event.BingoUnlockGroupEvent;
import com.aiyostudio.bingo.i18n.I18n;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * @author Blank038
 */
public class BingoListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onUnlockGroup(BingoUnlockGroupEvent event) {
        event.getPlayer().sendMessage(I18n.getStrAndHeader("unlock-group")
                .replace("%groupName%", event.getGroupCache().getName()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBingoQuestComplete(BingoQuestCompleteEvent event) {
        event.getPlayer().sendMessage(I18n.getStrAndHeader("quest-complete")
                .replace("%questName%", event.getQuestCache().getQuestName()));
    }
}
