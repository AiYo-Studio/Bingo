package com.aiyostudio.bingo;

import com.aiyostudio.bingo.api.event.DataSourceInitializeEvent;
import com.aiyostudio.bingo.cacheframework.manager.CacheManager;
import com.aiyostudio.bingo.command.BingoCommand;
import com.aiyostudio.bingo.config.DataSourceConfig;
import com.aiyostudio.bingo.config.DefaultConfig;
import com.aiyostudio.bingo.dao.AbstractDataSourceImpl;
import com.aiyostudio.bingo.dao.IDataSource;
import com.aiyostudio.bingo.hook.placeholders.PlaceholderHook;
import com.aiyostudio.bingo.i18n.I18n;
import com.aiyostudio.bingo.listen.BingoListener;
import com.aiyostudio.bingo.listen.PlayerListener;
import com.aiyostudio.bingo.listen.QuestTriggerListener;
import com.aiyostudio.bingo.service.IModelService;
import com.aiyostudio.bingo.task.UnlockGroupTask;
import com.aiyostudio.bingo.util.ScriptUtil;
import com.aystudio.core.bukkit.plugin.AyPlugin;
import org.bukkit.Bukkit;
import org.quartz.SchedulerException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
public class Bingo extends AyPlugin {
    private static Bingo instance;

    @Override
    public void onEnable() {
        instance = this;
        try {
            CacheManager.initializeCronScheduler();
        } catch (SchedulerException e) {
            this.getLogger().severe(e.toString());
        }

        this.getConsoleLogger().setPrefix("&f[&bBingo&f] ");
        this.getConsoleLogger().log(false, " ");
        this.getConsoleLogger().log(false, "&6   Bingo (AiYo Studio) &av" + this.getDescription().getVersion());
        this.getConsoleLogger().log(false, " ");
        this.getConsoleLogger().log(false, "&b  * &f载入模块列表");
        // initialize pluign
        this.loadConfig();
        this.initializeDataSource();
        this.initializeHandler();

        this.getConsoleLogger().log(false, "&e    + &f原版模块: &aON");
        this.initializeServices();
        this.registerHookPlugins();
        this.runTasks();

        this.getConsoleLogger().log(false, " ");
    }

    public void loadConfig() {
        DefaultConfig.initialize();

        File storageFolder = new File(this.getDataFolder(), "storage");
        try {
            Files.createDirectories(storageFolder.toPath());

            File playerData = new File(this.getDataFolder(), "playerData");
            if (playerData.exists()) {
                File to = new File(storageFolder, "playerData");
                Files.move(playerData.toPath(), to.toPath());
            }

            CacheManager.initialize();
        } catch (IOException | SchedulerException e) {
            this.getLogger().severe(e.toString());
        }
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
        Bukkit.getPluginManager().registerEvents(new QuestTriggerListener(), this);
        Bukkit.getPluginManager().registerEvents(new BingoListener(), this);

        ScriptUtil.initScriptEngine();
    }

    private void initializeServices() {
        String[] services = {
                "com.aiyostudio.bingo.model.pixelmon.legacy.PixelmonLegacyModelServiceImpl",
                "com.aiyostudio.bingo.model.pixelmon.nat.PixelmonNativeModelServiceImpl"
        };
        for (String serviceClassURI : services) {
            try {
                Class<? extends IModelService> c = (Class<? extends IModelService>) Class.forName(serviceClassURI);
                Method method = c.getMethod("run");
                method.invoke(c.newInstance());
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                this.getLogger().severe(e.toString());
            } catch (ClassNotFoundException | InvocationTargetException ignored) {
            }
        }
    }

    private void registerHookPlugins() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderHook().register();
        }
    }

    private void runTasks() {
        Bukkit.getScheduler().runTaskTimer(this, new UnlockGroupTask(), 60L, 60L);
    }

    public static Bingo getInstance() {
        return instance;
    }
}
