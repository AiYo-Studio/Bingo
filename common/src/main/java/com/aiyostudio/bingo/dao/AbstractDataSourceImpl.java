package com.aiyostudio.bingo.dao;

import com.aiyostudio.bingo.config.DataSourceConfig;
import com.aiyostudio.bingo.dao.impl.MysqlDataSourceImpl;
import com.aiyostudio.bingo.dao.impl.SQLiteDataSourceImpl;
import com.aiyostudio.bingo.dao.impl.YamlDataSourceImpl;
import com.aiyostudio.bingo.enums.DataSourceType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
@Getter
public abstract class AbstractDataSourceImpl implements IDataSource {
    private final DataSourceType dataSourceType;
    private final DataSourceConfig dataSourceConfig;
    @Setter
    private String dataSourceTag;
    protected final Map<String, Date> jobDateMap = new HashMap<>();

    public AbstractDataSourceImpl(DataSourceType sourceType, DataSourceConfig dataSourceConfig) {
        this.dataSourceConfig = dataSourceConfig;
        this.dataSourceType = sourceType;
    }

    @Override
    public Date getJobResetDate(String jobKey) {
        return this.jobDateMap.getOrDefault(jobKey, null);
    }

    public static IDataSource of(DataSourceConfig config) {
        IDataSource dataSource = null;
        switch (config.getType().toLowerCase()) {
            case "sqlite":
                dataSource = new SQLiteDataSourceImpl(config);
                break;
            case "mysql":
                dataSource = new MysqlDataSourceImpl(config);
                break;
            case "unknown":
                break;
            case "yaml":
            default:
                dataSource = new YamlDataSourceImpl(config);
                break;
        }
        return dataSource;
    }
}
