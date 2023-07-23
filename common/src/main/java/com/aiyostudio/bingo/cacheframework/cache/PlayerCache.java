package com.aiyostudio.bingo.cacheframework.cache;

import com.aiyostudio.bingo.cacheframework.manager.CacheManager;
import com.aiyostudio.bingo.enums.QuestStatus;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
public class PlayerCache {
    private final UUID uniqueId;
    private final List<String> claimed = new ArrayList<>();
    private final Map<String, QuestProgressCache> progress = new HashMap<>();

    public PlayerCache(UUID uuid, FileConfiguration data) {
        this.uniqueId = uuid;
        this.claimed.addAll(data.getStringList("claimed"));
        if (data.contains("progress")) {
            ConfigurationSection section = data.getConfigurationSection("progress");
            for (String key : section.getKeys(false)) {
                progress.put(key, new QuestProgressCache(section.getConfigurationSection(key)));
            }
        }
    }

    public boolean isCompleted(String... questId) {
        for (String key : questId) {
            if (!this.progress.containsKey(key) || this.progress.get(key).getQuestStatus() == QuestStatus.PROGRESS) {
                return false;
            }
        }
        return true;
    }

    public void createQuestProgress(String questId) {
        this.progress.put(questId, new QuestProgressCache());
    }

    public void addQuestProgress(String questId, String progressId, int count) {
        if (this.progress.containsKey(questId) && CacheManager.hasQuest(questId)) {
            QuestProgressCache cache = this.progress.get(questId);
            if (cache.getQuestStatus() == QuestStatus.PROGRESS) {
                cache.addProgress(progressId, count);
                cache.check(CacheManager.getQuestCache(questId));
            }
        }
    }

    public void addQuestClaimed(String qusetId) {
        this.claimed.add(qusetId);
    }

    public FileConfiguration toConfiguration() {
        FileConfiguration data = new YamlConfiguration();
        data.set("claimed", this.claimed);
        ConfigurationSection section = new YamlConfiguration();
        progress.forEach(section::set);
        data.set("progress", section);
        return data;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @Getter
    public static class QuestProgressCache {
        private QuestStatus questStatus;

        public QuestProgressCache() {
            this.questStatus = QuestStatus.PROGRESS;
        }

        public QuestProgressCache(ConfigurationSection section) {

        }

        public void addProgress(String progressId, int count) {

        }

        public void check(QuestCache questCache) {

        }
    }
}
