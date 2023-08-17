package com.aiyostudio.bingo.dao.impl;

import com.aiyostudio.bingo.Bingo;
import com.aiyostudio.bingo.cacheframework.cache.PlayerCache;
import com.aiyostudio.bingo.config.DataSourceConfig;
import com.aiyostudio.bingo.dao.AbstractDataSourceImpl;
import com.aiyostudio.bingo.enums.DataSourceType;
import com.aystudio.core.bukkit.interfaces.CustomExecute;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
public class SQLiteDataSourceImpl extends AbstractDataSourceImpl {

    public SQLiteDataSourceImpl(DataSourceConfig dataSourceConfig) {
        super(DataSourceType.SQLITE, dataSourceConfig);
        String[] array = {
                "CREATE TABLE IF NOT EXISTS bingo_users (user VARCHAR(36) NOT NULL, data TEXT, locked INT, PRIMARY KEY ( user ));",
                "CREATE TABLE IF NOT EXISTS bingo_jobs (id INTEGER PRIMARY KEY AUTOINCREMENT, job VARCHAR(100), date DATETIME);"
        };
        for (String sql : array) {
            this.connect((s) -> {
                try {
                    s.executeUpdate();
                } catch (SQLException e) {
                    Bingo.getInstance().getLogger().severe(e.toString());
                }
            }, sql);
        }
        this.loadJobResetData();
    }

    @Override
    public PlayerCache getPlayerCache(UUID uniqueId) {
        AtomicReference<FileConfiguration> atomicReference = new AtomicReference<>();
        this.connect((statement) -> {
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
        this.connect((statement) -> {
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
        this.connect((statement) -> {
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
        this.connect((statement) -> {
            try {
                statement.executeUpdate();
            } catch (SQLException e) {
                Bingo.getInstance().getLogger().severe(e.toString());
            }
        }, String.format("UPDATE bingo_users SET locked=%s WHERE user='%s'", (lock ? 1 : 0), uniqueId.toString()));
    }

    @Override
    public void loadJobResetData(String... keys) {
        this.jobDateMap.clear();
        if (keys.length == 0) {
            this.connect((statement) -> {
                try {
                    ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        this.jobDateMap.put(resultSet.getString(1), resultSet.getDate(2));
                    }
                    resultSet.close();
                } catch (SQLException e) {
                    Bingo.getInstance().getLogger().severe(e.toString());
                }
            }, "SELECT job, MAX(date) AS date FROM bingo_jobs GROUP BY job ORDER BY date DESC;");
        } else {
            for (String key : keys) {
                this.connect((statement) -> {
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
                }, "SELECT job, MAX(date) AS date FROM bingo_jobs WHERE job=? ORDER BY date DESC;");
            }
        }
    }

    @Override
    public void resetJobCache(String jobKey, Date date) {
        this.jobDateMap.put(jobKey, date);
        this.connect((statement) -> {
            try {
                statement.setString(1, jobKey);
                statement.setDate(2, new java.sql.Date(date.getTime()));
            } catch (SQLException e) {
                Bingo.getInstance().getLogger().severe(e.toString());
            }
        }, "INSERT INTO bingo_jobs (job, date) VALUES (?, ?);");
    }

    public void connect(CustomExecute<PreparedStatement> executeModel, String sql) {
        try (Connection connection = DriverManager.getConnection(this.getDataSourceConfig().getUrl())) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            executeModel.run(preparedStatement);
            preparedStatement.close();
        } catch (SQLException e) {
            Bingo.getInstance().getLogger().severe(e.toString());
        }
    }
}
