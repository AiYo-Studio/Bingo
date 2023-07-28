package com.aiyostudio.bingo.cacheframework.cache;

import com.aiyostudio.bingo.api.BingoApi;
import com.aiyostudio.bingo.api.event.BingoQuestCompleteEvent;
import com.aiyostudio.bingo.cacheframework.manager.CacheManager;
import com.aiyostudio.bingo.enums.QuestStatus;
import com.aiyostudio.bingo.hook.placeholders.PlaceholderHook;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
@Getter
public class PlayerCache {
    private final UUID uniqueId;
    private final List<String> claimed = new ArrayList<>(), unlockGroup = new ArrayList<>(),
            receivedSegmentRewards = new ArrayList<>();
    private final Map<String, QuestProgressCache> progress = new HashMap<>();
    @Setter
    private boolean newData;

    public PlayerCache(UUID uuid, FileConfiguration data) {
        if (data.contains("new")) {
            this.newData = true;
        }
        this.uniqueId = uuid;
        this.claimed.addAll(data.getStringList("claimed"));
        if (data.contains("progress")) {
            ConfigurationSection section = data.getConfigurationSection("progress");
            for (String key : section.getKeys(false)) {
                progress.put(key, new QuestProgressCache(section.getConfigurationSection(key)));
            }
        }
        this.unlockGroup.addAll(data.getStringList("unlockGroup"));
        this.receivedSegmentRewards.addAll(data.getStringList("receivedSegmentRewards"));

        this.unlockGroup.forEach(s -> {
            if (CacheManager.hasGroupCache(s)) {
                CacheManager.getGroupCache(s).getUnlockList().stream()
                        .filter(v -> !progress.containsKey(v))
                        .forEach(this::createQuestProgress);
            }
        });
    }

    public boolean isReceived(String segmentRewardId) {
        return this.receivedSegmentRewards.contains(segmentRewardId);
    }

    public void addReceivedRewardKey(String segmentRewardId) {
        this.receivedSegmentRewards.add(segmentRewardId);
    }

    public boolean hasQuest(String questId) {
        return this.progress.containsKey(questId);
    }

    public boolean hasGroup(String groupId) {
        return this.unlockGroup.contains(groupId);
    }

    public void addGroup(String groupId) {
        if (this.hasGroup(groupId)) {
            return;
        }
        this.unlockGroup.add(groupId);
    }

    public boolean isCompleted(String... questId) {
        for (String key : questId) {
            if (!this.progress.containsKey(key) || this.progress.get(key).getQuestStatus() == QuestStatus.PROGRESS) {
                return false;
            }
        }
        return true;
    }

    public boolean isClaimed(String claimKey) {
        return this.claimed.contains(claimKey);
    }

    public void createQuestProgress(String questId) {
        if (this.progress.containsKey(questId) || !CacheManager.hasQuest(questId)) {
            return;
        }
        this.progress.put(questId, new QuestProgressCache(questId));
    }

    public void addQuestProgress(String questType, String condition, int count) {
        for (Map.Entry<String, QuestProgressCache> entry : this.progress.entrySet()) {
            if (entry.getValue().questStatus == QuestStatus.COMPLETED) {
                continue;
            }
            this.addQuestProgress(entry.getKey(), questType, condition, count);
        }
    }

    public void addQuestProgress(String questId, String type, String condition, int count) {
        if (this.progress.containsKey(questId) && CacheManager.hasQuest(questId)) {
            QuestProgressCache cache = this.progress.get(questId);
            if (cache.getQuestStatus() == QuestStatus.PROGRESS) {
                cache.addProgress(type, condition, count);

                QuestCache questCache = CacheManager.getQuestCache(questId);
                if (cache.check(questCache)) {
                    Player target = Bukkit.getPlayer(this.getUniqueId());

                    this.addQuestClaimed(questId);

                    Map<String, List<String>> map = BingoApi.getCommandsOfNodes(target, questCache);
                    if (!map.isEmpty()) {
                        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                            entry.getValue().forEach((command) -> {
                                String last = PlaceholderHook.format(target, command);
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), last.replace("%player%", target.getName()));
                            });
                        }
                    }

                    BingoQuestCompleteEvent event = new BingoQuestCompleteEvent(target, questCache);
                    Bukkit.getPluginManager().callEvent(event);
                }
            }
        }
    }

    public QuestProgressCache getQuestProgress(String questId) {
        if (this.progress.containsKey(questId) && CacheManager.hasQuest(questId)) {
            return this.progress.get(questId);
        }
        return null;
    }

    public double getQuestProgressPct(String questId) {
        if (this.progress.containsKey(questId) && CacheManager.hasQuest(questId)) {
            return this.progress.get(questId).getQuestProgressPct(CacheManager.getQuestCache(questId));
        }
        return 0.0;
    }

    public void addQuestClaimed(String qusetId) {
        this.claimed.add(qusetId);
    }

    public FileConfiguration toConfiguration() {
        FileConfiguration data = new YamlConfiguration();
        data.set("claimed", this.claimed);
        data.set("unlockGroup", this.unlockGroup);
        data.set("receivedSegmentRewards", this.receivedSegmentRewards);
        ConfigurationSection section = new YamlConfiguration();
        progress.forEach((k, v) -> section.set(k, v.toSection()));
        data.set("progress", section);
        return data;
    }

    @Getter
    public static class QuestProgressCache {
        private final Map<String, ProgressEntry> progressEntryMap = new HashMap<>();
        private final String questId;
        private QuestStatus questStatus;

        public QuestProgressCache(String questId) {
            this.questStatus = QuestStatus.PROGRESS;
            this.questId = questId;
            this.calProgress();
        }

        public QuestProgressCache(ConfigurationSection section) {
            this.questId = section.getString("questId");
            this.questStatus = QuestStatus.valueOf(section.getString("status"));
            if (section.contains("progressList")) {
                for (String key : section.getConfigurationSection("progressList").getKeys(false)) {
                    ConfigurationSection entrySection = section.getConfigurationSection("progressList." + key);
                    ProgressEntry progressEntry = new ProgressEntry(entrySection.getString("type"),
                            entrySection.getString("condition"), entrySection.getInt("value"));
                    this.progressEntryMap.put(key, progressEntry);
                }
            }
            this.calProgress();
        }

        private void calProgress() {
            QuestCache questCache = CacheManager.getQuestCache(this.questId);
            if (questCache != null && this.progressEntryMap.isEmpty()) {
                ProgressEntry progressEntry = new ProgressEntry(questCache.getQuestType(), questCache.getCondition(), 0);
                this.progressEntryMap.put(questCache.getQuestType() + "-" + questCache.getCondition(), progressEntry);
            }
        }

        public void addProgress(String type, String condition, int count) {
            for (Map.Entry<String, ProgressEntry> entry : this.progressEntryMap.entrySet()) {
                ProgressEntry progressEntry = entry.getValue();
                if (progressEntry.getType().equals(type) && ("*".equals(condition) || condition.equals(progressEntry.getCondition()) || "*".equals(progressEntry.getCondition()))) {
                    progressEntry.setValue(progressEntry.getValue() + count);
                }
            }
        }

        public boolean check(QuestCache questCache) {
            String progressId = questCache.getQuestType() + "-" + questCache.getCondition();
            if (this.getQuestStatus() == QuestStatus.PROGRESS && this.progressEntryMap.containsKey(progressId)
                    && this.progressEntryMap.get(progressId).getValue() >= questCache.getAmount()) {
                this.setQuestStatus(QuestStatus.COMPLETED);
                return true;
            }
            return false;
        }

        public void setQuestStatus(QuestStatus questStatus) {
            this.questStatus = questStatus;
        }

        public ConfigurationSection toSection() {
            ConfigurationSection section = new YamlConfiguration();
            section.set("status", this.questStatus.name());
            for (Map.Entry<String, ProgressEntry> entry : this.progressEntryMap.entrySet()) {
                section.set("progressList." + entry.getKey() + ".type", entry.getValue().type);
                section.set("progressList." + entry.getKey() + ".condition", entry.getValue().condition);
                section.set("progressList." + entry.getKey() + ".value", entry.getValue().value);
            }
            return section;
        }

        public double getQuestProgressPct(QuestCache questCache) {
            if (this.questStatus == QuestStatus.COMPLETED) {
                return 1.0;
            }
            double size = this.progressEntryMap.size();
            int sum = this.progressEntryMap.values().stream().mapToInt(s -> s.value).sum();
            return Math.min(1.0, (sum / size) / questCache.getAmount());
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ProgressEntry {
        private final String type, condition;
        @Setter
        private int value;
    }
}
