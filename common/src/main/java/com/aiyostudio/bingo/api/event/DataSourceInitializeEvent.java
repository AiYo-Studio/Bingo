package com.aiyostudio.bingo.api.event;

import com.aiyostudio.bingo.dao.IDataSource;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
public class DataSourceInitializeEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Getter
    @Setter
    private IDataSource dataSource;

    public DataSourceInitializeEvent(IDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
