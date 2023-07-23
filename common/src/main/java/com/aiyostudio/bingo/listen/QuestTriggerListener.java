package com.aiyostudio.bingo.listen;

import com.aiyostudio.bingo.api.BingoApi;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-23
 */
public class QuestTriggerListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        BingoApi.submit(event.getPlayer(), "break", event.getBlock().getType().name(), 1);
    }
}
