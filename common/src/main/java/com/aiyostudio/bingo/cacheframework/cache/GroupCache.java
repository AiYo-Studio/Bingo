package com.aiyostudio.bingo.cacheframework.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @author Blank038
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
@Getter
@AllArgsConstructor
public class GroupCache {
    private final List<String> conditionList, unlockList;
    private final String name;
}
