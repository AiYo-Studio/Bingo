package com.aiyostudio.bingo.cacheframework.manager;

import com.aiyostudio.bingo.Bingo;
import com.aiyostudio.bingo.cacheframework.cache.*;
import com.aiyostudio.bingo.cron.CronJob;
import com.aiyostudio.bingo.dao.IDataSource;
import com.aiyostudio.bingo.hook.HookState;
import com.aiyostudio.bingo.util.TextUtil;
import com.aystudio.core.bukkit.interfaces.CustomExecute;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
public class CacheManager {
    private static final Map<UUID, PlayerCache> PLAYER_CACHE_MAP = new ConcurrentHashMap<>();
    private static final Map<String, QuestCache> QUEST_CACHE_MAP = new HashMap<>();
    private static final Map<String, ViewCache> VIEW_CACHE_MAP = new HashMap<>();
    private static final Map<String, NodeCache> NODE_CACHE_MAP = new HashMap<>();
    private static final Map<String, GroupCache> GROUP_CACHE_MAP = new HashMap<>();
    private static final Map<String, JobCache> JOB_CACHE_MAP = new HashMap<>();
    private static Scheduler scheduler;

    private static final CustomExecute<File> QUEST_LOAD_SCRIPT = (file) -> {
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        for (String questKey : data.getKeys(false)) {
            try {
                CacheManager.QUEST_CACHE_MAP.put(questKey, new QuestCache(data.getConfigurationSection(questKey)));
            } catch (Exception e) {
                Bingo.getInstance().getLogger().severe("加载任务 '" + questKey + "' 失败 (文件: " + file.getName() + "): " + e.getMessage());
                e.printStackTrace();
            }
        }
    };
    private static IDataSource dataSource;


    public static void initialize() throws SchedulerException {
        try {
            CacheManager.loadQuestCache();
        } catch (Exception e) {
            Bingo.getInstance().getLogger().severe("加载任务缓存阶段失败: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            CacheManager.loadViewCache();
        } catch (Exception e) {
            Bingo.getInstance().getLogger().severe("加载视图缓存阶段失败: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            CacheManager.loadGroupCache();
        } catch (Exception e) {
            Bingo.getInstance().getLogger().severe("加载任务组缓存阶段失败: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            CacheManager.loadNodeCache();
        } catch (Exception e) {
            Bingo.getInstance().getLogger().severe("加载节点缓存阶段失败: " + e.getMessage());
            e.printStackTrace();
        }
        CacheManager.loadJobs();
    }

    public static void initializeCronScheduler() throws SchedulerException {
        if (scheduler == null) {
            StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
            schedulerFactory.initialize(Bingo.getInstance().getResource("com/aiyostudio/bingo/quartz.properties"));
            scheduler = schedulerFactory.getScheduler();
        }
    }

    private static void loadQuestCache() {
        CacheManager.QUEST_CACHE_MAP.clear();

        File questFolder = new File(Bingo.getInstance().getDataFolder(), "quests");
        if (!questFolder.exists()) {
            Bingo.getInstance().saveResource("quests/default.yml", "quests/default.yml");
            Bingo.getInstance().saveResource("quests/rare.yml", "quests/rare.yml");
        }
        CacheManager.loadQuestCache(questFolder);
    }

    private static void loadQuestCache(File targetFile) {
        if (targetFile.isDirectory()) {
            File[] files = targetFile.listFiles();
            if (files == null) return;
            for (File child : files) {
                CacheManager.loadQuestCache(child);
            }
        } else {
            CacheManager.QUEST_LOAD_SCRIPT.run(targetFile);
        }
    }

    private static void loadViewCache() {
        CacheManager.VIEW_CACHE_MAP.clear();

        File viewFolder = new File(Bingo.getInstance().getDataFolder(), "view");
        if (!viewFolder.exists()) {
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1)) {
                Bingo.getInstance().saveResource("view/default.yml", "view/default.yml");
                Bingo.getInstance().saveResource("view/rare.yml", "view/rare.yml");
            } else {
                Bingo.getInstance().saveResource("view/legacy/default.yml", "view/default.yml");
                Bingo.getInstance().saveResource("view/legacy/rare.yml", "view/rare.yml");
            }
        }
        File[] viewFiles = viewFolder.listFiles();
        if (viewFiles == null) return;
        for (File file : viewFiles) {
            try {
                String name = file.getName().substring(0, file.getName().indexOf(".yml"));
                CacheManager.VIEW_CACHE_MAP.put(name, new ViewCache(name, YamlConfiguration.loadConfiguration(file)));
            } catch (Exception e) {
                Bingo.getInstance().getLogger().severe("加载视图文件 '" + file.getName() + "' 失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void loadNodeCache() {
        CacheManager.NODE_CACHE_MAP.clear();
        Bingo.getInstance().saveResource("node.yml", "node.yml", false, (file) -> {
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            for (String key : configuration.getKeys(false)) {
                try {
                    ConfigurationSection section = configuration.getConfigurationSection(key);
                    CacheManager.NODE_CACHE_MAP.put(key, new NodeCache(section.getString("permission")));
                } catch (Exception e) {
                    Bingo.getInstance().getLogger().severe("加载节点 '" + key + "' 失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private static void loadGroupCache() {
        CacheManager.GROUP_CACHE_MAP.clear();

        File groupFolder = new File(Bingo.getInstance().getDataFolder(), "groups");
        if (!groupFolder.exists()) {
            Bingo.getInstance().saveResource("groups/default.yml", "groups/default.yml");
            Bingo.getInstance().saveResource("groups/rare.yml", "groups/rare.yml");
        }
        File[] groupFiles = groupFolder.listFiles();
        if (groupFiles == null) return;
        for (File file : groupFiles) {
            try {
                String name = file.getName().substring(0, file.getName().indexOf(".yml"));
                FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                GroupCache groupCache = new GroupCache(yaml.getStringList("condition"),
                        yaml.getStringList("unlock"), TextUtil.formatHexColor(yaml.getString("name")));
                CacheManager.GROUP_CACHE_MAP.put(name, groupCache);
            } catch (Exception e) {
                Bingo.getInstance().getLogger().severe("加载任务组文件 '" + file.getName() + "' 失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void loadJobs() throws SchedulerException {
        CacheManager.JOB_CACHE_MAP.clear();
        scheduler.clear();

        File jobFolder = new File(Bingo.getInstance().getDataFolder(), "jobs");
        if (!jobFolder.exists()) {
            Bingo.getInstance().saveResource("jobs/example.yml", "jobs/example.yml");
        }

        File[] jobFiles = jobFolder.listFiles();
        if (jobFiles == null) return;
        for (File i : jobFiles) {
            try {
                String key = i.getName().substring(0, i.getName().indexOf(".yml"));
                FileConfiguration data = YamlConfiguration.loadConfiguration(i);

                CacheManager.JOB_CACHE_MAP.put(key, new JobCache(data));

                // start cron task
                JobDetail jobDetail = JobBuilder.newJob(CronJob.class)
                        .withIdentity(key, "bingoJobGroup")
                        .usingJobData("JobKey", key)
                        .build();
                Trigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity(key + "Trigger", "bingoTriggerGroup")
                        .withSchedule(CronScheduleBuilder.cronSchedule(data.getString("expression")))
                        .build();
                scheduler.scheduleJob(jobDetail, trigger);
            } catch (Exception e) {
                Bingo.getInstance().getLogger().severe("加载 Job 文件 '" + i.getName() + "' 失败: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (!scheduler.isStarted()) {
            scheduler.start();
        }
    }

    public static IDataSource getDataSource() {
        return dataSource;
    }

    public static void setDataSource(IDataSource dataSource) {
        CacheManager.dataSource = dataSource;
    }



    public static void loadCache(UUID uuid) {
        CacheManager.unloadCache(uuid);
        // load PlayerCache for target player.
        PLAYER_CACHE_MAP.put(uuid, dataSource.getPlayerCache(uuid));
    }

    public static void unloadCache(UUID uuid) {
        PlayerCache playerCache = CacheManager.PLAYER_CACHE_MAP.remove(uuid);
        if (playerCache != null) {
            dataSource.save(playerCache, 0);
        }
    }

    public static PlayerCache getPlayerCache(UUID uuid) {
        return CacheManager.PLAYER_CACHE_MAP.getOrDefault(uuid, null);
    }

    public static boolean hasPlayerCache(UUID uuid) {
        return CacheManager.PLAYER_CACHE_MAP.containsKey(uuid);
    }

    public static Map<UUID, PlayerCache> getAllPlayerCaches() {
        return CacheManager.PLAYER_CACHE_MAP;
    }

    public static QuestCache getQuestCache(String questId) {
        return CacheManager.QUEST_CACHE_MAP.getOrDefault(questId, null);
    }

    public static boolean hasQuest(String questId) {
        return CacheManager.QUEST_CACHE_MAP.containsKey(questId);
    }

    public static Map<String, QuestCache> getQuestCaches() {
        return QUEST_CACHE_MAP;
    }

    public static boolean hasViewCache(String viewKey) {
        return CacheManager.VIEW_CACHE_MAP.containsKey(viewKey);
    }

    public static ViewCache getViewCache(String viewKey) {
        return CacheManager.VIEW_CACHE_MAP.getOrDefault(viewKey, null);
    }

    public static Map<String, ViewCache> getViewCaches() {
        return VIEW_CACHE_MAP;
    }

    public static boolean hasGroupCache(String groupId) {
        return CacheManager.GROUP_CACHE_MAP.containsKey(groupId);
    }

    public static GroupCache getGroupCache(String groupId) {
        return CacheManager.GROUP_CACHE_MAP.getOrDefault(groupId, null);
    }

    public static Map<String, GroupCache> getAllGroup() {
        return CacheManager.GROUP_CACHE_MAP;
    }

    public static boolean hasNodeCache(String nodeId) {
        return CacheManager.NODE_CACHE_MAP.containsKey(nodeId);
    }

    public static NodeCache getNodeCache(String nodeId) {
        return CacheManager.NODE_CACHE_MAP.getOrDefault(nodeId, null);
    }

    public static Map<String, NodeCache> getAllNodes() {
        return CacheManager.NODE_CACHE_MAP;
    }

    public static Map<String, JobCache> getJobCacheMap() {
        return CacheManager.JOB_CACHE_MAP;
    }

    public static void saveAllPlayerCache() {
        if (HookState.onSave) {
            return;
        }
        HookState.onSave = true;
        try {
            Iterator<Map.Entry<UUID, PlayerCache>> iterator = CacheManager.getAllPlayerCaches().entrySet().iterator();
            while (iterator.hasNext()) {
                CacheManager.getDataSource().save(iterator.next().getValue(), 1);
            }
        } finally {
            HookState.onSave = false;
        }
    }
}
