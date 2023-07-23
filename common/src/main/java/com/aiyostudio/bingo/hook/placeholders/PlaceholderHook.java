package com.aiyostudio.bingo.hook;

import com.aiyostudio.bingo.Bingo;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

/**
 * @author Blank038
 */
public class PlaceholderHook extends PlaceholderExpansion {
    private static PlaceholderHook instance;

    public PlaceholderHook() {
        instance = this;
    }

    @Override
    public String onPlaceholderRequest(Player p, String params) {
        if (params.startsWith("total_progress_")) {
            
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

    public static String format(Player target, String line) {
        if (instance == null) {
            return line;
        }
        return PlaceholderAPI.setPlaceholders(target, line);
    }
}
