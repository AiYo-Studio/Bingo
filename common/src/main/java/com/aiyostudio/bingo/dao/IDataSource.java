package com.aiyostudio.bingo.dao;

import com.aiyostudio.bingo.cacheframework.cache.PlayerCache;

import java.util.UUID;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
public interface IDataSource {

    /**
     * Get the PlayerCache for the target player.
     *
     * @param uniqueId player uniqueId.
     * @return PlayerCache result.
     */
    PlayerCache getPlayerCache(UUID uniqueId);

    /**
     * Save player cache.
     *
     * @param playerCache player cache.
     */
    void save(PlayerCache playerCache, int locked);

    boolean isLocked(UUID uniqueId);

    void setLock(UUID uniqueId, boolean lock);
}
