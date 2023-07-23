package com.aiyostudio.bingo.config;

import com.aiyostudio.bingo.Bingo;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
public class DefaultConfig {
    private static DefaultConfig instance;

    public DefaultConfig() {
        instance = this;
    }

    public void initConfig() {
        Bingo.getInstance().saveDefaultConfig();
        Bingo.getInstance().reloadConfig();
    }

    public static void initialize() {
        if (instance == null) {
            instance = new DefaultConfig();
        }
        instance.initConfig();
    }

    public static FileConfiguration getConfig() {
        if (instance == null) {
            return null;
        }
        return Bingo.getInstance().getConfig();
    }
}
