package com.aiyostudio.bingo.cacheframework.cache;

import com.aiyostudio.bingo.enums.JobType;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * @author AiYo Studio - Blank038
 * @since 1.0.2 - Blank038 - 2023-08-11
 */
@Getter
public class JobCache {
    private final String expression;
    private final List<String> questList, segmentRewards;
    private final JobType jobType;
    private final int random;

    public JobCache(FileConfiguration data) {
        this.jobType = JobType.valueOf(data.getString("type").toUpperCase());
        this.expression = data.getString("expression");
        this.questList = Lists.newArrayList(data.getString("quests").split(","));
        this.segmentRewards = Lists.newArrayList(data.getString("segmentRewards").split(","));
        this.random = data.getInt("random");
    }
}
