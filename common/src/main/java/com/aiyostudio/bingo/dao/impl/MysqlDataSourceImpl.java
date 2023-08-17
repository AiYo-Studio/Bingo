package com.aiyostudio.bingo.dao.impl;

import com.aiyostudio.bingo.Bingo;
import com.aiyostudio.bingo.cacheframework.cache.PlayerCache;
import com.aiyostudio.bingo.config.DataSourceConfig;
import com.aiyostudio.bingo.dao.AbstractDataSourceImpl;
import com.aiyostudio.bingo.enums.DataSourceType;
import com.aystudio.core.bukkit.util.mysql.MySqlStorageHandler;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
public class MysqlDataSourceImpl extends AbstractDataSourceImpl {
    private static MySqlStorageHandler dataSourceHandler;

    public MysqlDataSourceImpl(DataSourceConfig config) {
        super(DataSourceType.MYSQL, config);
        String[] array = {
                "CREATE TABLE IF NOT EXISTS bingo_users (user VARCHAR(36) NOT NULL, data TEXT, locked INT, PRIMARY KEY ( user )) ENGINE=InnoDB DEFAULT CHARSET=utf8;",
                "CREATE TABLE IF NOT EXISTS bingo_jobs (id INT NOT NULL AUTO_INCREMENT, job VARCHAR(100), date DATETIME, PRIMARY KEY ( id )) ENGINE=InnoDB DEFAULT CHARSET=utf8;"
        };
        dataSourceHandler = new MySqlStorageHandler(Bingo.getInstance(), config.getUrl(), config.getUser(), config.getPassword(), array);
        dataSourceHandler.setReconnectionQueryTable("bingo_users");
        this.loadJobResetData();
    }

    @Override
    public PlayerCache getPlayerCache(UUID uniqueId) {
        AtomicReference<FileConfiguration> atomicReference = new AtomicReference<>();
        dataSourceHandler.connect((statement) -> {
            try {
                statement.setString(1, uniqueId.toString());
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    String data = resultSet.getString("data");
                    if (data != null) {
                        FileConfiguration object = new YamlConfiguration();
                        object.loadFromString(new String(Base64.getDecoder().decode(data.getBytes(StandardCharsets.UTF_8))));
                        atomicReference.set(object);
                    }
                }
                resultSet.close();
            } catch (SQLException | InvalidConfigurationException e) {
                Bingo.getInstance().getLogger().severe(e.toString());
            }
        }, "SELECT data FROM bingo_users WHERE user=?");
        if (atomicReference.get() == null) {
            FileConfiguration object = new YamlConfiguration();
            object.set("new", true);
            atomicReference.set(object);
        }
        return new PlayerCache(uniqueId, atomicReference.get());
    }

    @Override
    public void save(PlayerCache playerCache, int locked) {
        FileConfiguration object = playerCache.toConfiguration();
        String text = new String(Base64.getEncoder().encode(object.saveToString().getBytes(StandardCharsets.UTF_8)));
        String sql = String.format(playerCache.isNewData() ? "INSERT INTO bingo_users (user,data,locked) VALUES (?,?,%s)" : "UPDATE bingo_users SET data=?, locked='%s' WHERE user=?", locked);
        dataSourceHandler.connect((statement) -> {
            try {
                if (playerCache.isNewData()) {
                    statement.setString(1, playerCache.getUniqueId().toString());
                    statement.setString(2, text);
                    playerCache.setNewData(false);
                } else {
                    statement.setString(1, text);
                    statement.setString(2, playerCache.getUniqueId().toString());
                }
                statement.executeUpdate();
            } catch (SQLException e) {
                Bingo.getInstance().getLogger().severe(e.toString());
            }
        }, sql);
    }

    @Override
    public boolean isLocked(UUID uniqueId) {
        AtomicReference<Boolean> result = new AtomicReference<>();
        result.set(false);
        dataSourceHandler.connect((statement) -> {
            try {
                statement.setString(1, uniqueId.toString());
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    int locked = resultSet.getInt("locked");
                    if (locked == 1) {
                        result.set(true);
                        break;
                    }
                }
                resultSet.close();
            } catch (SQLException e) {
                Bingo.getInstance().getLogger().severe(e.toString());
            }
        }, "SELECT locked FROM bingo_users WHERE user=?");
        return result.get();
    }

    @Override
    public void setLock(UUID uniqueId, boolean lock) {
        dataSourceHandler.connect((statement) -> {
            try {
                statement.executeUpdate();
            } catch (SQLException e) {
                Bingo.getInstance().getLogger().severe(e.toString());
            }
        }, String.format("UPDATE bingo_users SET locked=%s WHERE user='%s'", (lock ? 1 : 0), uniqueId.toString()));
    }

    @Override
    public void loadJobResetData(String... keys) {
        if (keys.length == 0) {
            dataSourceHandler.connect((statement) -> {
                try {
                    ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        this.jobDateMap.put(resultSet.getString(1), resultSet.getDate(2));
                    }
                    resultSet.close();
                } catch (SQLException e) {
                    Bingo.getInstance().getLogger().severe(e.toString());
                }
            }, "SELECT job, MAX(date) AS latest_date FROM bingo_jobs GROUP BY job;");
        } else {
            for (String key : keys) {
                dataSourceHandler.connect((statement) -> {
                    try {
                        statement.setString(1, key);

                        ResultSet resultSet = statement.executeQuery();
                        while (resultSet.next()) {
                            this.jobDateMap.put(resultSet.getString(1), resultSet.getDate(2));
                        }
                        resultSet.close();
                    } catch (SQLException e) {
                        Bingo.getInstance().getLogger().severe(e.toString());
                    }
                }, "SELECT job, MAX(date) AS latest_date FROM bingo_jobs WHERE job=? GROUP BY job;");
            }
        }
    }

    @Override
    public void resetJobCache(String jobKey, Date date) {
        this.jobDateMap.put(jobKey, date);
        dataSourceHandler.connect((statement) -> {
            try {
                statement.setString(1, jobKey);
                statement.setDate(2, new java.sql.Date(date.getTime()));
            } catch (SQLException e) {
                Bingo.getInstance().getLogger().severe(e.toString());
            }
        }, "INSERT INTO bingo_jobs (job, date) VALUES (?, ?)");
    }
}
