package com.aiyostudio.bingo;

import com.aiyostudio.bingo.api.event.DataSourceInitializeEvent;
import com.aiyostudio.bingo.cacheframework.manager.CacheManager;
import com.aiyostudio.bingo.command.BingoCommand;
import com.aiyostudio.bingo.config.DataSourceConfig;
import com.aiyostudio.bingo.config.DefaultConfig;
import com.aiyostudio.bingo.dao.AbstractDataSourceImpl;
import com.aiyostudio.bingo.dao.IDataSource;
import com.aiyostudio.bingo.i18n.I18n;
import com.aiyostudio.bingo.listen.PlayerListener;
import com.aiyostudio.bingo.service.IModelService;
import com.aystudio.core.bukkit.plugin.AyPlugin;
import org.bukkit.Bukkit;

import java.util.ServiceLoader;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
public class Bingo extends AyPlugin {
    private static Bingo instance;

    @Override
    public void onEnable() {
        instance = this;
        this.loadConfig();
        this.initializeDataSource();
        this.initializeHandler();
        this.initializeServices();
    }

    public void loadConfig() {
        DefaultConfig.initialize();
        CacheManager.initialize();
        new I18n(DefaultConfig.getConfig().getString("language", "zh_CN"));
    }

    private void initializeDataSource() {
        DataSourceConfig config = new DataSourceConfig(this.getConfig().getConfigurationSection("data-option"));
        IDataSource dataSource = AbstractDataSourceImpl.of(config);

        DataSourceInitializeEvent event = new DataSourceInitializeEvent(dataSource);
        Bukkit.getPluginManager().callEvent(event);

        CacheManager.setDataSource(event.getDataSource());
    }

    private void initializeHandler() {
        this.getCommand("bingo").setExecutor(new BingoCommand());
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
    }

    private void initializeServices() {
        ServiceLoader<IModelService> serviceLoader = ServiceLoader.load(IModelService.class);
        serviceLoader.forEach(IModelService::run);
    }

    public static Bingo getInstance() {
        return instance;
    }
}
