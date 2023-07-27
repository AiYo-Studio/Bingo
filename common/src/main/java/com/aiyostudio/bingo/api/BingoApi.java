package com.aiyostudio.bingo.api;

import com.aiyostudio.bingo.Bingo;
import com.aiyostudio.bingo.cacheframework.cache.QuestCache;
import com.aiyostudio.bingo.cacheframework.manager.CacheManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
public class BingoApi {

    public static Map<String, List<String>> getCommandsOfNodes(Player player, QuestCache questCache) {
        List<String> nodes = CacheManager.getAllNodes().entrySet().stream()
                .filter((entry) -> entry.getValue().getPermission() == null || player.hasPermission(entry.getValue().getPermission()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (nodes.isEmpty()) {
            return new HashMap<>(0);
        }
        return questCache.getCommands().entrySet().stream()
                .filter((s) -> nodes.contains(s.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static void submit(Player target, String questType, String condition, int amount) {
        if (Bingo.getInstance().getConfig().getBoolean("quest-trigger-log")) {
            Bingo.getInstance().getLogger().info(String.format("Player: %s, QuestType: %s, Condition: %s, Amount: %s",
                    target.getName(), questType, condition, amount));
        }
        if (target != null && CacheManager.hasPlayerCache(target.getUniqueId())) {
            CacheManager.getPlayerCache(target.getUniqueId()).addQuestProgress(questType, condition, amount);
        }
    }
}
