package com.aiyostudio.bingo.dao.impl;

import com.aiyostudio.bingo.Bingo;
import com.aiyostudio.bingo.cacheframework.cache.PlayerCache;
import com.aiyostudio.bingo.config.DataSourceConfig;
import com.aiyostudio.bingo.dao.AbstractDataSourceImpl;
import com.aiyostudio.bingo.enums.DataSourceType;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
public class YamlDataSourceImpl extends AbstractDataSourceImpl {

    public YamlDataSourceImpl(DataSourceConfig config) {
        super(DataSourceType.YAML, config);
    }

    @Override
    public PlayerCache getPlayerCache(UUID uniqueId) {
        File file = new File(Bingo.getInstance().getDataFolder(), "playerData");
        if (!file.exists()) {
            file.mkdir();
        }
        return new PlayerCache(uniqueId, YamlConfiguration.loadConfiguration(new File(file, uniqueId.toString() + ".yml")));
    }

    @Override
    public void save(PlayerCache playerCache) {
        File file = new File(Bingo.getInstance().getDataFolder(), "playerData");
        if (!file.exists()) {
            file.mkdir();
        }
        try {
            playerCache.toConfiguration().save(new File(file, playerCache.getUniqueId().toString() + ".yml"));
        } catch (IOException e) {
            Bingo.getInstance().getLogger().severe(e.toString());
        }
    }

    @Override
    public boolean isLocked(UUID uniqueId) {
        return false;
    }

    @Override
    public void setLock(UUID uniqueId, boolean lock) {
    }
}
