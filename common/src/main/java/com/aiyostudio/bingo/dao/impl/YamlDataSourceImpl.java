package com.aiyostudio.bingo.dao.impl;

import com.aiyostudio.bingo.Bingo;
import com.aiyostudio.bingo.cacheframework.cache.PlayerCache;
import com.aiyostudio.bingo.config.DataSourceConfig;
import com.aiyostudio.bingo.dao.AbstractDataSourceImpl;
import com.aiyostudio.bingo.enums.DataSourceType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
    public void save(PlayerCache playerCache, int locked) {
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

    @Override
    public void loadJobResetData(String... keys) {
        this.jobDateMap.clear();

        File file = new File(Bingo.getInstance().getDataFolder(), "jobData");
        if (!file.exists()) {
            file.mkdir();
        }

        for (String key : keys) {
            File f = new File(file, key + ".yml");
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    Bingo.getInstance().getLogger().severe(e.toString());
                }
            }
            FileConfiguration data = YamlConfiguration.loadConfiguration(f);
            this.jobDateMap.put(key, new Date(data.getLong("resetDate")));
        }
    }

    @Override
    public void resetJobCache(String jobKey, Date date) {
        this.jobDateMap.put(jobKey, date);

        File file = new File(Bingo.getInstance().getDataFolder(), "jobData");
        if (!file.exists()) {
            file.mkdir();
        }

        File f = new File(file, jobKey + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(f);
        data.set("resetDate", date.getTime());
        try {
            data.save(f);
        } catch (IOException e) {
            Bingo.getInstance().getLogger().severe(e.toString());
        }
    }
}
