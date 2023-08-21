package com.aiyostudio.bingo.task;

import com.aiyostudio.bingo.api.event.BingoUnlockGroupEvent;
import com.aiyostudio.bingo.cacheframework.cache.GroupCache;
import com.aiyostudio.bingo.cacheframework.cache.PlayerCache;
import com.aiyostudio.bingo.cacheframework.manager.CacheManager;
import com.aiyostudio.bingo.util.ScriptUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * @author Blank038
 */
public class UnlockGroupTask implements Runnable {

    @Override
    public void run() {
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!CacheManager.hasPlayerCache(target.getUniqueId())) {
                continue;
            }
            PlayerCache playerCache = CacheManager.getPlayerCache(target.getUniqueId());
            for (Map.Entry<String, GroupCache> entry : CacheManager.getAllGroup().entrySet()) {
                if (playerCache.hasGroup(entry.getKey())) {
                    continue;
                }
                if (ScriptUtil.detectionCondition(target, entry.getValue().getConditionList())) {
                    playerCache.addGroup(entry.getKey());
                    entry.getValue().getUnlockList().forEach(playerCache::createQuestProgress);

                    BingoUnlockGroupEvent event = new BingoUnlockGroupEvent(target, entry.getKey(), entry.getValue());
                    Bukkit.getPluginManager().callEvent(event);
                }
            }
            // check invaild jobs
            playerCache.checkInvalidJobs();
        }
    }
}
