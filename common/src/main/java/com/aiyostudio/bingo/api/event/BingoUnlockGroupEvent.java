package com.aiyostudio.bingo.api.event;

import com.aiyostudio.bingo.cacheframework.cache.GroupCache;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * @author Blank038
 */
@Getter
public class BingoUnlockGroupEvent extends PlayerEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final String groupId;
    private final GroupCache groupCache;

    public BingoUnlockGroupEvent(Player who, String groupId, GroupCache groupCache) {
        super(who);
        this.groupId = groupId;
        this.groupCache = groupCache;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
