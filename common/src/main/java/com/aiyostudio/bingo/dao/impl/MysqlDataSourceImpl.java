package com.aiyostudio.bingo.dao.impl;

import com.aiyostudio.bingo.Bingo;
import com.aiyostudio.bingo.cacheframework.cache.PlayerCache;
import com.aiyostudio.bingo.config.DataSourceConfig;
import com.aiyostudio.bingo.dao.AbstractDataSourceImpl;
import com.aiyostudio.bingo.enums.DataSourceType;
import com.aystudio.core.bukkit.util.mysql.MySqlStorageHandler;

import java.sql.SQLException;
import java.util.UUID;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
public class MysqlDataSourceImpl extends AbstractDataSourceImpl {
    private static MySqlStorageHandler dataSourceHandler;

    public MysqlDataSourceImpl(DataSourceConfig config) {
        super(DataSourceType.MYSQL, config);
        String[] array = {
                "CREATE TABLE IF NOT EXISTS bingo_users (user VARCHAR(36) NOT NULL, data TEXT, locked INT, PRIMARY KEY ( user ));"
        };
        dataSourceHandler = new MySqlStorageHandler(Bingo.getInstance(), config.getUrl(), config.getUser(), config.getPassword(), array);
        dataSourceHandler.setReconnectionQueryTable("bingo_users");
    }

    @Override
    public PlayerCache getPlayerCache(UUID uniqueId) {
        return null;
    }

    @Override
    public void save(PlayerCache playerCache) {

    }

    @Override
    public boolean isLocked(UUID uniqueId) {
        return false;
    }

    @Override
    public void setLock(UUID uniqueId, boolean lock) {
        dataSourceHandler.connect((statement) -> {
            try {
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, String.format("UPDATE bingo_users SET locked=%s WHERE user='%s'", (lock ? 1 : 0), uniqueId.toString()));
    }
}
