package com.aiyostudio.bingo.cacheframework.cache;

import com.aiyostudio.bingo.util.TextUtil;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
@Getter
public class QuestCache {
    private final String questType, questName;
    private final List<String> requireQuestIds = new ArrayList<>();
    private final int amount;
    private final List<String> appendLore = new ArrayList<>(),
            conditions = new ArrayList<>();
    private final Map<String, List<String>> commands = new HashMap<>();

    public QuestCache(ConfigurationSection section) {
        this.questName = TextUtil.formatHexColor(section.getString("name"));
        this.questType = section.getString("type");
        if (section.isString("require")) {
            this.requireQuestIds.add(section.getString("require"));
        } else if (section.isList("require")) {
            this.requireQuestIds.addAll(section.getStringList("require"));
        }
        // Backward compatibility for older configs.
        if (this.requireQuestIds.isEmpty()) {
            String legacy = section.getString("pre-quest",
                    section.getString("preQuest", section.getString("require-quest")));
            if (legacy != null && !legacy.isEmpty()) {
                this.requireQuestIds.add(legacy);
            }
        }
        if (section.isString("condition")) {
            this.conditions.add(section.getString("condition"));
        } else if (section.isList("condition")) {
            this.conditions.addAll(section.getStringList("condition"));
        }
        this.amount = section.getInt("amount");
        this.appendLore.addAll(section.getStringList("appendLore"));
        if (section.contains("commands")) {
            for (String key : section.getConfigurationSection("commands").getKeys(false)) {
                this.commands.put(key, section.getStringList("commands." + key));
            }
        }
    }
}
