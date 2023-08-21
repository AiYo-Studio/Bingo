package com.aiyostudio.bingo.cacheframework.cache;

import com.aiyostudio.bingo.util.TextUtil;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
@Getter
public class ViewCache {
    private final List<String> requireQuests = new ArrayList<>(), alwaysCondition = new ArrayList<>();
    private final List<FileConfiguration> questItems = new ArrayList<>(),
            displayItems = new ArrayList<>(),
            stateItems = new ArrayList<>();
    private final String viewTitle, viewId, viewType;
    private final int viewSize, requireCount;
    private final FileConfiguration originConfiguration;

    public ViewCache(String viewId, FileConfiguration data) {
        this.viewId = viewId;
        this.viewTitle = TextUtil.formatHexColor(data.getString("title"));
        this.viewSize = data.getInt("size");
        this.viewType = data.getString("viewType", "default");
        this.requireCount = data.getInt("require-count", -1);
        this.requireQuests.addAll(data.getStringList("require-quests"));
        this.alwaysCondition.addAll(data.getStringList("always-condition"));

        data.getList("quest-item").forEach((s) -> {
            FileConfiguration configuration = new YamlConfiguration();
            configuration.addDefaults((Map<String, Object>) s);
            questItems.add(configuration);
        });
        data.getList("state-item").forEach((s) -> {
            FileConfiguration configuration = new YamlConfiguration();
            configuration.addDefaults((Map<String, Object>) s);
            stateItems.add(configuration);
        });
        data.getList("items").forEach((s) -> {
            FileConfiguration configuration = new YamlConfiguration();
            configuration.addDefaults((Map<String, Object>) s);
            displayItems.add(configuration);
        });
        this.originConfiguration = data;
    }
}
