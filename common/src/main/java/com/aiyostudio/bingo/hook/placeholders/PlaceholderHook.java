package com.aiyostudio.bingo.hook.placeholders;

import com.aiyostudio.bingo.Bingo;
import com.aiyostudio.bingo.cacheframework.cache.GroupCache;
import com.aiyostudio.bingo.cacheframework.cache.PlayerCache;
import com.aiyostudio.bingo.cacheframework.manager.CacheManager;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-23
 */
public class PlaceholderHook extends PlaceholderExpansion {
    private static final Map<String, PlaceholderInterface<Player, String>> PLACEHOLDER_INTERFACE_MAP = new HashMap<>();
    private static PlaceholderHook instance;

    public PlaceholderHook() {
        instance = this;

        PlaceholderHook.PLACEHOLDER_INTERFACE_MAP.put("quest_progress", (p, v) -> {
            if (p != null && CacheManager.hasPlayerCache(p.getUniqueId())) {
                int value = (int) (CacheManager.getPlayerCache(p.getUniqueId()).getQuestProgressPct(v) * 100.0);
                return String.valueOf(Math.min(100, value));
            }
            return "0";
        });

        PlaceholderHook.PLACEHOLDER_INTERFACE_MAP.put("group_progress", (p, v) -> {
            if (p != null && CacheManager.hasPlayerCache(p.getUniqueId()) && CacheManager.hasGroupCache(v)
                    && CacheManager.hasPlayerCache(p.getUniqueId())) {
                GroupCache cache = CacheManager.getGroupCache(v);
                PlayerCache playerCache = CacheManager.getPlayerCache(p.getUniqueId());
                int size = cache.getUnlockList().size();
                double sum = cache.getUnlockList().stream()
                        .mapToDouble(playerCache::getQuestProgressPct)
                        .sum();
                return String.valueOf(Math.min(100, (int) ((sum / size) * 100.0)));
            }
            return "0";
        });
    }

    @Override
    public String onPlaceholderRequest(Player p, String params) {
        int index = params.lastIndexOf('_');
        String param = params.substring(0, index);
        if (PlaceholderHook.PLACEHOLDER_INTERFACE_MAP.containsKey(param)) {
            return PlaceholderHook.PLACEHOLDER_INTERFACE_MAP.get(param).run(p, params.substring(index + 1));
        }
        return "";
    }

    @Override
    public String getIdentifier() {
        return "bingo";
    }

    @Override
    public String getAuthor() {
        return "AiYo Studio";
    }

    @Override
    public String getVersion() {
        return Bingo.getInstance().getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    public static String format(Player target, String line) {
        if (instance == null) {
            return line;
        }
        return PlaceholderAPI.setPlaceholders(target, line);
    }

    @FunctionalInterface
    public interface PlaceholderInterface<T, V> {

        String run(T p, V v);
    }
}
