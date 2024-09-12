package com.aiyostudio.bingo.handler.format;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author Blank038
 */
public class Formatter {
    private static final Map<String, BiFunction<Player, String, String>> FORMATTERS = new HashMap<>();

    static {
        FORMATTERS.put("default", (player, s) -> player == null ? s : s.replace("%player%", player.getName()));
    }

    public static void register(String label, BiFunction<Player, String, String> function) {
        FORMATTERS.put(label, function);
    }

    public static String format(Player player, String text) {
        if (FORMATTERS.isEmpty()) return text;
        return FORMATTERS.values().stream()
                .reduce(text, (result, function) -> function.apply(player, result), (r1, r2) -> r1);
    }
}
