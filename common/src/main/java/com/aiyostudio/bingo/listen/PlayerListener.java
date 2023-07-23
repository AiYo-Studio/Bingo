package com.aiyostudio.bingo.listen;

import com.aiyostudio.bingo.Bingo;
import com.aiyostudio.bingo.cacheframework.manager.CacheManager;
import com.aiyostudio.bingo.i18n.I18n;
import com.aiyostudio.bingo.util.TaskUtil;
import com.aystudio.core.bukkit.thread.BlankThread;
import com.aystudio.core.bukkit.thread.ThreadProcessor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
public class PlayerListener implements Listener {
    private final Bingo instance = Bingo.getInstance();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (this.instance.getConfig().getBoolean("data-option.pull-notify")) {
            event.getPlayer().sendMessage(I18n.getStrAndHeader("pulling-start"));
        }

        ThreadProcessor.crateTask(this.instance, new BlankThread(10) {
            private int count;

            @Override
            public void run() {
                Player player = event.getPlayer();
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }
                if (!CacheManager.getDataSource().isLocked(player.getUniqueId())) {
                    loadData(player);
                    this.cancel();
                } else {
                    count++;
                    if (count > PlayerListener.this.instance.getConfig().getInt("data-option.time-out")) {
                        loadData(player);
                        this.cancel();
                    }
                }
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUniqued = event.getPlayer().getUniqueId();
        TaskUtil.runAsyncTask(() -> CacheManager.unloadCache(playerUniqued));
    }

    private void loadData(Player player) {
        CacheManager.getDataSource().setLock(player.getUniqueId(), true);
        CacheManager.loadCache(player.getUniqueId());
        if (this.instance.getConfig().getBoolean("data-option.pull-notify")) {
            player.sendMessage(I18n.getStrAndHeader("pulling-completed"));
        }
    }
}
