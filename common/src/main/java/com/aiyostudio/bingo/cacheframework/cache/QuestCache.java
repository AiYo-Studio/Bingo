package com.aiyostudio.bingo.cacheframework.cache;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
@Getter
public class QuestCache {
    private final String questType, condition;
    private final int amount;

    public QuestCache(ConfigurationSection section) {
        this.questType = section.getString("type");
        this.condition = section.getString("condition");
        this.amount = section.getInt("amount");
    }
}
