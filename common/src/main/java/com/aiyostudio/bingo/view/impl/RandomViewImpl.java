package com.aiyostudio.bingo.view.impl;

import com.aiyostudio.bingo.cacheframework.cache.QuestCache;
import com.aiyostudio.bingo.cacheframework.cache.ViewCache;
import com.aiyostudio.bingo.cacheframework.manager.CacheManager;
import com.aiyostudio.bingo.config.DefaultConfig;
import com.aiyostudio.bingo.handler.format.Formatter;
import com.aiyostudio.bingo.i18n.I18n;
import com.aiyostudio.bingo.util.TextUtil;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Blank038
 */
public class RandomViewImpl extends DefaultViewImpl {
    private final List<String> quests;

    public RandomViewImpl(Player player, ViewCache viewCache) {
        super(player, viewCache);
        this.quests = this.viewCache.getRequireQuests();
    }

    @Override
    public void call() {
       this.prerequisites = () -> {
           int requireCount = this.getViewCache().getRequireCount();
           if ((requireCount == -1 && this.quests.stream().allMatch(this.playerCache::hasQuest))
                   || (requireCount > -1 && this.quests.stream().filter(this.playerCache::hasQuest).count() >= requireCount)) {
               return true;
           } else {
               player.sendMessage(I18n.getStrAndHeader("view-locked"));
               return false;
           }
       };
    }

    @Override
    public void initializeQuestItem() {
        List<String> quests = this.viewCache.getRequireQuests().stream()
                .filter(this.playerCache::hasQuest)
                .collect(Collectors.toList());
        List<Integer> slots = this.viewCache.getOriginConfiguration().getIntegerList("random-item.slots");
        ConfigurationSection config = this.getViewCache().getOriginConfiguration().getConfigurationSection("random-item.item");
        for (int i = 0; i < slots.size() && i < quests.size(); i++) {
            String questId = quests.get(i);
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
                if (line.contains("%appendLore%")) {
                    lore.addAll(questCache.getAppendLore());
                } else {
                    lore.add(line);
                }
            }
            lore.replaceAll((s) -> TextUtil.formatHexColor(Formatter.format(this.player, s))
                    .replace("%questName%", questName)
                    .replace("%progress%", progress)
                    .replace("%pct%", String.valueOf(pctInt)));
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            if (config.contains("nbt")) {
                NBTItem nbtItem = new NBTItem(itemStack);
                ReadWriteNBT compound = NBT.parseNBT(config.getString("nbt"));
                nbtItem.mergeCompound(compound);
                itemStack = nbtItem.getItem();
            }

            model.setItem(slots.get(i), itemStack);
        }
    }
}
