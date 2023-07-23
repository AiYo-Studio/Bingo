package com.aiyostudio.bingo.view;

import com.aiyostudio.bingo.Bingo;
import com.aiyostudio.bingo.cacheframework.cache.PlayerCache;
import com.aiyostudio.bingo.cacheframework.cache.QuestCache;
import com.aiyostudio.bingo.cacheframework.cache.ViewCache;
import com.aiyostudio.bingo.cacheframework.manager.CacheManager;
import com.aiyostudio.bingo.config.DefaultConfig;
import com.aiyostudio.bingo.hook.placeholders.PlaceholderHook;
import com.aiyostudio.bingo.util.TextUtil;
import com.aystudio.core.bukkit.util.common.CommonUtil;
import com.aystudio.core.bukkit.util.inventory.GuiModel;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
public class BingoView {

    public static void open(Player player, ViewCache viewCache) {
        PlayerCache playerCache = CacheManager.getPlayerCache(player.getUniqueId());

        GuiModel model = new GuiModel(viewCache.getViewTitle(), viewCache.getViewSize());
        model.registerListener(Bingo.getInstance());
        model.setCloseRemove(true);

        BingoView.initializeDisplayItem(player, model, viewCache);
        BingoView.initializeQuestItem(player, model, playerCache, viewCache);
        BingoView.initializeStateItem(player, model, playerCache, viewCache);

        model.execute((e) -> {
            e.setCancelled(true);
            if (e.getClickedInventory() == e.getInventory()) {

            }
        });
        model.openInventory(player);
    }

    private static void initializeDisplayItem(Player target, GuiModel model, ViewCache viewCache) {
        viewCache.getDisplayItems().forEach(config -> {
            ItemStack itemStack = new ItemStack(Material.valueOf(config.getString("type")), 1);
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1)) {
                ((Damageable) itemMeta).setDamage((short) config.getInt("data"));
                if (config.contains("custom-data")) {
                    itemMeta.setCustomModelData(config.getInt("custom-data"));
                }
            } else {
                itemStack.setDurability((short) config.getInt("data"));
            }
            itemMeta.setDisplayName(TextUtil.formatHexColor(config.getString("name")));
            List<String> lore = new ArrayList<>();
            for (String line : config.getStringList("lore")) {
                lore.add(TextUtil.formatHexColor(PlaceholderHook.format(target, line)));
            }
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
            if (config.contains("view")) {
                NBTItem nbtItem = new NBTItem(itemStack);
                nbtItem.setString("BingoView", config.getString("view"));
                itemStack = nbtItem.getItem();
            }
            for (int slot : CommonUtil.formatSlots(config.getString("slot"))) {
                model.setItem(slot, itemStack);
            }
        });
    }

    private static void initializeQuestItem(Player target, GuiModel model, PlayerCache playerCache, ViewCache viewCache) {
        viewCache.getQuestItems().forEach(config -> {
            String questId = config.getString("quest");
            QuestCache questCache = CacheManager.getQuestCache(questId);
            String questName = questCache.getQuestName();
            double pct = playerCache.getQuestProgressPct(questId);
            int length = DefaultConfig.getConfig().getInt("progress.length"), split = (int) (pct * length), pctInt = (int) (pct * 100);
            String originalProgressText = DefaultConfig.getConfig().getString("progress.text"),
                    header = originalProgressText.substring(0, split), footer = originalProgressText.substring(split),
                    progress = TextUtil.formatHexColor(DefaultConfig.getConfig().getString("progress.complete") + header
                            + DefaultConfig.getConfig().getString("progress.undone") + footer);

            boolean completed = playerCache.isCompleted(questId);

            ItemStack itemStack = new ItemStack(Material.valueOf(config.getString("type")), 1);
            if (completed) {
                itemStack.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
            }
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (completed) {
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1)) {
                ((Damageable) itemMeta).setDamage((short) config.getInt("data"));
                if (config.contains("custom-data")) {
                    itemMeta.setCustomModelData(config.getInt("custom-data"));
                }
            } else {
                itemStack.setDurability((short) config.getInt("data"));
            }
            itemMeta.setDisplayName(TextUtil.formatHexColor(config.getString("name")
                    .replace("%questName%", questName).replace("%progress%", progress)
                    .replace("%pct%", String.valueOf(pctInt))));
            List<String> lore = new ArrayList<>();
            for (String line : config.getStringList("lore")) {
                lore.add(TextUtil.formatHexColor(PlaceholderHook.format(target, line))
                        .replace("%questName%", questName)
                        .replace("%progress%", progress)
                        .replace("%pct%", String.valueOf(pctInt)));
            }
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
            for (int slot : CommonUtil.formatSlots(config.getString("slot"))) {
                model.setItem(slot, itemStack);
            }
        });
    }

    private static void initializeStateItem(Player target, GuiModel model, PlayerCache playerCache, ViewCache viewCache) {
        viewCache.getStateItems().forEach(s -> {
            String[] quests = s.getStringList("quests").toArray(new String[0]);
            int stateIndex = 2;
            if (playerCache.isCompleted(quests)) {
                stateIndex = playerCache.isClaimed(s.getString("claimKey")) ? 0 : 1;
            }
            Map<String, Object> map = (Map<String, Object>) s.getList("state").get(stateIndex);
            FileConfiguration config = new YamlConfiguration();
            config.addDefaults(map);

            // Create state item.
            ItemStack itemStack = new ItemStack(Material.valueOf(config.getString("type")), 1);
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1)) {
                ((Damageable) itemMeta).setDamage((short) config.getInt("data"));
                if (config.contains("custom-data")) {
                    itemMeta.setCustomModelData(config.getInt("custom-data"));
                }
            } else {
                itemStack.setDurability((short) config.getInt("data"));
            }
            itemMeta.setDisplayName(TextUtil.formatHexColor(config.getString("name")));
            List<String> lore = new ArrayList<>();
            for (String line : config.getStringList("lore")) {
                lore.add(TextUtil.formatHexColor(PlaceholderHook.format(target, line)));
            }
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            NBTItem nbtItem = new NBTItem(itemStack);
            nbtItem.setString("BingoQuestReward", s.getString("claimKey"));
            itemStack = nbtItem.getItem();

            for (int slot : CommonUtil.formatSlots(s.getString("slot"))) {
                model.setItem(slot, itemStack);
            }
        });
    }
}