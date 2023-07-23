package com.aiyostudio.bingo.config;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
@Getter
public class DataSourceConfig {
    private final String user, password, url, type;
    private final int timeout;
    private final boolean pullNotify;

    public DataSourceConfig(ConfigurationSection section) {
        this.type = section.getString("type");
        this.url = section.getString("url");
        this.user = section.getString("user");
        this.password = section.getString("password");
        this.timeout = section.getInt("timeout");
        this.pullNotify = section.getBoolean("pull-notify");
    }
}
