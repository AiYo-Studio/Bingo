package com.aiyostudio.bingo.view.impl;

import com.aiyostudio.bingo.cacheframework.cache.PlayerCache;
import com.aiyostudio.bingo.cacheframework.cache.QuestCache;
import com.aiyostudio.bingo.cacheframework.cache.ViewCache;
import com.aiyostudio.bingo.cacheframework.manager.CacheManager;
import com.aiyostudio.bingo.config.DefaultConfig;
import com.aiyostudio.bingo.hook.placeholders.PlaceholderHook;
import com.aiyostudio.bingo.i18n.I18n;
import com.aiyostudio.bingo.util.TextUtil;
import com.aiyostudio.bingo.view.AbstractView;
import com.aystudio.core.bukkit.util.common.CommonUtil;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import org.bukkit.Bukkit;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Blank038
 */
public class DefaultViewImpl extends AbstractView {
    private Map<String, List<String>[]> stateMap;
    protected boolean checkRequireQuests = true;

    public DefaultViewImpl(Player player, ViewCache viewCache) {
        super(player, viewCache);
    }

    @Override
    public void call() {
    }

    @Override
    public void open() {
        if (checkRequireQuests && viewCache.getRequireQuests().stream().anyMatch(s -> !playerCache.hasQuest(s))) {
            player.sendMessage(I18n.getStrAndHeader("view-locked"));
            return;
        }
        super.open();
    }

    @Override
    public void onPreInit() {
        PlayerCache playerCache = CacheManager.getPlayerCache(player.getUniqueId());
        if (viewCache.getRequireQuests().stream().anyMatch(s -> !playerCache.hasQuest(s))) {
            player.sendMessage(I18n.getStrAndHeader("view-locked"));
            return;
        }
        this.initializeDisplayItem();
        this.initializeQuestItem();
        this.stateMap = this.initializeStateItem();
    }

    @Override
    public void onPostInit() {
        this.getModel().execute((e) -> {
            e.setCancelled(true);
            if (e.getClickedInventory() == e.getInventory()) {
                ItemStack itemStack = e.getCurrentItem();
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    return;
                }
                Player clicker = (Player) e.getWhoClicked();
                NBTItem nbtItem = new NBTItem(itemStack);
                if (nbtItem.hasTag("BingoView")) {
                    String viewId = nbtItem.getString("BingoView");
                    if (viewId.equals(viewCache.getViewId())) {
                        return;
                    }
                    this.open();
                } else if (nbtItem.hasTag("BingoQuestReward")) {
                    String rewardId = nbtItem.getString("BingoQuestReward");
                    if (!this.stateMap.containsKey(rewardId)) {
                        return;
                    }
                    List<String>[] array = this.stateMap.get(rewardId);
                    PlayerCache tempPlayerCache = CacheManager.getPlayerCache(player.getUniqueId());
                    if (tempPlayerCache.isReceived(rewardId)) {
                        clicker.sendMessage(I18n.getStrAndHeader("reward-received"));
                        return;
                    }
                    if (array[0].stream().allMatch(tempPlayerCache::isCompleted)) {
                        tempPlayerCache.addReceivedRewardKey(rewardId);
                        array[1].forEach((command) -> {
                            String last = PlaceholderHook.format(clicker, command);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), last.replace("%player%", clicker.getName()));
                        });
                        clicker.sendMessage(I18n.getStrAndHeader("gotten-reward"));
                        this.open();
                    } else {
                        clicker.sendMessage(I18n.getStrAndHeader("quest-undone"));
                    }
                }
            }
        });
    }

    @Override
    public void initializeDisplayItem() {
        this.getViewCache().getDisplayItems().forEach(config -> {
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
                lore.add(TextUtil.formatHexColor(PlaceholderHook.format(this.getPlayer(), line)));
            }
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
            if (config.contains("view")) {
                NBTItem nbtItem = new NBTItem(itemStack);
                nbtItem.setString("BingoView", config.getString("view"));
                if (config.contains("nbt")) {
                    ReadWriteNBT compound = NBT.parseNBT(config.getString("nbt"));
                    nbtItem.mergeCompound(compound);
                }
                itemStack = nbtItem.getItem();
            }
            for (int slot : CommonUtil.formatSlots(config.getString("slot"))) {
                this.getModel().setItem(slot, itemStack);
            }
        });
    }

    @Override
    public void initializeQuestItem() {
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
                if (line.contains("%appendLore%")) {
                    lore.addAll(questCache.getAppendLore());
                } else {
                    lore.add(line);
                }
            }
            lore.replaceAll((s) -> TextUtil.formatHexColor(PlaceholderHook.format(this.player, s))
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

            for (int slot : CommonUtil.formatSlots(config.getString("slot"))) {
                model.setItem(slot, itemStack);
            }
        });
    }

    @Override
    public Map<String, List<String>[]> initializeStateItem() {
        Map<String, List<String>[]> result = new HashMap<>();
        viewCache.getStateItems().forEach(s -> {
            String claimKey = s.getString("claimKey");
            String[] quests = s.getStringList("quests").toArray(new String[0]);

            int stateIndex = 2;
            if (playerCache.isCompleted(quests)) {
                stateIndex = playerCache.isReceived(claimKey) ? 0 : 1;
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
                lore.add(TextUtil.formatHexColor(PlaceholderHook.format(this.player, line)));
            }
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            NBTItem nbtItem = new NBTItem(itemStack);
            nbtItem.setString("BingoQuestReward", claimKey);
            if (config.contains("nbt")) {
                ReadWriteNBT compound = NBT.parseNBT(config.getString("nbt"));
                nbtItem.mergeCompound(compound);
            }
            itemStack = nbtItem.getItem();

            for (int slot : CommonUtil.formatSlots(s.getString("slot"))) {
                model.setItem(slot, itemStack);
            }

            List<String>[] array = new List[]{s.getStringList("quests"), s.getStringList("commands")};
            result.put(claimKey, array);
        });
        return result;
    }
}
