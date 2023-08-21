package com.aiyostudio.bingo.view;

import com.aiyostudio.bingo.Bingo;
import com.aiyostudio.bingo.cacheframework.cache.PlayerCache;
import com.aiyostudio.bingo.cacheframework.cache.ViewCache;
import com.aiyostudio.bingo.cacheframework.manager.CacheManager;
import com.aiyostudio.bingo.registries.ViewRegistry;
import com.aiyostudio.bingo.util.ScriptUtil;
import com.aystudio.core.bukkit.util.inventory.GuiModel;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
@Getter
public abstract class AbstractView implements IView {
    protected final Player player;
    protected final ViewCache viewCache;
    protected final PlayerCache playerCache;
    protected GuiModel model;

    public AbstractView(Player player, ViewCache viewCache) {
        this.player = player;
        this.playerCache = CacheManager.getPlayerCache(player.getUniqueId());
        this.viewCache = viewCache;
        this.call();
    }

    public void open() {
        if (!ScriptUtil.detectionCondition(player, this.viewCache.getAlwaysCondition())) {
            this.player.sendMessage("view-condition-denied");
            return;
        }
        this.model = new GuiModel(viewCache.getViewTitle(), viewCache.getViewSize());
        this.model.registerListener(Bingo.getInstance());
        this.model.setCloseRemove(true);
        this.onPreInit();
        this.initializeDisplayItem();
        this.initializeQuestItem();
        this.initializeStateItem();
        this.onPostInit();
        model.openInventory(player);
    }

    public static AbstractView create(Player player, ViewCache viewCache) {
        Class<? extends AbstractView> modelClass = ViewRegistry.getView(viewCache.getViewType());
        try {
            Constructor<? extends AbstractView> constructor = modelClass.getConstructor(Player.class, ViewCache.class);
            return constructor.newInstance(player, viewCache);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            Bingo.getInstance().getLogger().severe(e.toString());
        }
        return null;
    }
}