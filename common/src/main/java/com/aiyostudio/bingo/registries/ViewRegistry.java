package com.aiyostudio.bingo.registries;

import com.aiyostudio.bingo.view.AbstractView;
import com.aiyostudio.bingo.view.impl.DefaultViewImpl;
import com.aiyostudio.bingo.view.impl.RandomViewImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Blank038
 */
public class ViewRegistry {
    private static final Map<String, Class<? extends AbstractView>> VIEW_REGISTRY = new HashMap<>();

    static {
        ViewRegistry.VIEW_REGISTRY.put("default", DefaultViewImpl.class);
        ViewRegistry.VIEW_REGISTRY.put("random", RandomViewImpl.class);
    }

    public static void register(String viewType, Class<? extends AbstractView> aClass) {
        if (ViewRegistry.VIEW_REGISTRY.containsKey(viewType)) {
            return;
        }
        ViewRegistry.VIEW_REGISTRY.put(viewType, aClass);
    }

    public static Class<? extends AbstractView> getView(String type) {
        return ViewRegistry.VIEW_REGISTRY.getOrDefault(type, ViewRegistry.VIEW_REGISTRY.get("default"));
    }
}
