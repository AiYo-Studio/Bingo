package com.aiyostudio.bingo.api.event;

import com.aiyostudio.bingo.cacheframework.cache.QuestCache;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * @author Blank038
 */
public class BingoQuestCompleteEvent extends PlayerEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Getter
    private final QuestCache questCache;

    public BingoQuestCompleteEvent(Player who, QuestCache questCache) {
        super(who);
        this.questCache = questCache;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
