package com.aiyostudio.bingo.task;

import com.aiyostudio.bingo.Bingo;
import com.aiyostudio.bingo.api.event.BingoUnlockGroupEvent;
import com.aiyostudio.bingo.cacheframework.cache.GroupCache;
import com.aiyostudio.bingo.cacheframework.cache.PlayerCache;
import com.aiyostudio.bingo.cacheframework.manager.CacheManager;
import com.aiyostudio.bingo.hook.placeholders.PlaceholderHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.List;
import java.util.Map;

/**
 * @author Blank038
 */
public class UnlockGroupTask implements Runnable {
    private static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("JavaScript");

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
                if (this.detectionCondition(target, entry.getValue().getConditionList())) {
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


    public boolean detectionCondition(Player player, List<String> conditions) {
        if (conditions.isEmpty()) {
            return true;
        }
        try {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < conditions.size(); i++) {
                if (i + 1 == conditions.size()) {
                    stringBuilder.append(conditions.get(i));
                } else {
                    stringBuilder.append(conditions.get(i)).append(" && ");
                }
            }
            return (boolean) SCRIPT_ENGINE.eval(PlaceholderHook.format(player, stringBuilder.toString()));
        } catch (Exception e) {
            Bingo.getInstance().getLogger().severe(" Condition is invalid " + e);
            return false;
        }
    }
}
