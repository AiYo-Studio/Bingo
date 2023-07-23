package com.aiyostudio.bingo.listen;

import com.aiyostudio.bingo.api.BingoApi;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.ItemStack;


/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-23
 */
public class QuestTriggerListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        BingoApi.submit(event.getPlayer(), "break", event.getBlock().getType().name(), 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        BingoApi.submit(event.getPlayer(), "place", event.getBlock().getType().name(), 1);
    }

    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent event) {
        if (event.getNewLevel() > event.getOldLevel()) {
            BingoApi.submit(event.getPlayer(), "level", String.valueOf(event.getNewLevel()), 1);
            BingoApi.submit(event.getPlayer(), "level_up", "*", event.getNewLevel() - event.getOldLevel());
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            LivingEntity entity = event.getEntity();
            String entityName = entity.getCustomName() != null ? entity.getCustomName() : entity.getType().name();
            BingoApi.submit(entity.getKiller(), "kill_entity", entityName, 1);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        ItemStack itemStack = event.getRecipe().getResult();
        String name = itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() ?
                itemStack.getItemMeta().getDisplayName() : itemStack.getType().name();
        BingoApi.submit((Player) event.getWhoClicked(), "craft_item", name, 1);
    }
}
