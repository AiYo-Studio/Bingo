package com.aiyostudio.bingo.model.pixelmon.legacy.listen.container;

import com.aiyostudio.bingo.api.BingoApi;
import com.pixelmonmod.pixelmon.api.events.*;
import com.pixelmonmod.pixelmon.api.events.pokemon.EVsGainedEvent;
import com.pixelmonmod.pixelmon.api.events.pokemon.SetNicknameEvent;
import com.pixelmonmod.pixelmon.api.events.quests.FinishQuestEvent;
import com.pixelmonmod.pixelmon.api.events.raids.EndRaidEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.minecraft.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-23
 */
public class ForgeEventExecutorContainer {

    private static void submitCustomQuestByType(Player player, String prefix, EnumSpecies species) {
        // 判定是否为神兽
        if (species.isLegendary()) {
            BingoApi.submit(player, prefix + "_type", "legendary", 1);
        } else if (species.isUltraBeast()) {
            BingoApi.submit(player, prefix + "_type", "ultrabeast", 1);
        }
    }

    /**
     * 玩家击败野生精灵
     */
    public static final Consumer<BeatWildPixelmonEvent> BEAT_WILD_PIXELMON = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUniqueID());
        EnumSpecies species = event.wpp.allPokemon[0].pokemon.getSpecies();
        // 提交击败任务
        BingoApi.submit(player, "beat_wild_pixelmon", species.name(), 1);
        submitCustomQuestByType(player, "beat_wild_pixelmon", species);
    };
    /*
     * 玩家击败巢穴精灵
     */
    public static final Consumer<EndRaidEvent> END_RAID_EVENT_CONSUMER = (event) -> {
        if (event.didRaidersWin()) {
            event.getAllyParticipants().forEach((participant) -> {
                if (participant instanceof PlayerParticipant) {
                    Player player = Bukkit.getPlayer((((PlayerParticipant) participant).player.getUniqueID()));
                    EnumSpecies species = event.getRaid().getSpecies();
                    BingoApi.submit(player, "beat_raid_governor", species.name(), 1);
                    submitCustomQuestByType(player, "beat_raid_governor", species);
                }
            });
        }
    };
    /**
     * 捕捉野外精灵
     */
    public static final Consumer<CaptureEvent.SuccessfulCapture> CAPTURE_PIXELMON = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUniqueID());
        EnumSpecies species = event.getPokemon().getSpecies();
        BingoApi.submit(player, "capture_pixelmon", species.name(), 1);
        submitCustomQuestByType(player, "capture_pixelmon", species);
    };
    /**
     * 捕捉巢穴精灵
     */
    public static final Consumer<CaptureEvent.SuccessfulRaidCapture> CAPTURE_RAID_PIXELMON = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUniqueID());
        EnumSpecies species = event.getRaidPokemon().getSpecies();
        BingoApi.submit(player, "capture_raid_pixelmon", species.name(), 1);
        submitCustomQuestByType(player, "capture_raid_pixelmon", species);
    };
    /**
     * 击败训练师
     */
    public static final Consumer<BeatTrainerEvent> BEAT_TRAINER = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUniqueID());
        BingoApi.submit(player, "beat_trainer", event.trainer.getName(), 1);
    };
    /**
     * 钓鱼
     */
    public static final Consumer<FishingEvent.Catch> FISHING_CATCH = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUniqueID());
        if (event.plannedSpawn == null) {
            return;
        }
        Entity entity = event.plannedSpawn.getOrCreateEntity();
        if (entity == null) {
            return;
        }
        BingoApi.submit(player, "fishing_catch", event.plannedSpawn.getOrCreateEntity().getName(), 1);
    };
    /**
     * 采摘树果
     */
    public static final Consumer<ApricornEvent.PickApricorn> PICK_APRICORN = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUniqueID());
        BingoApi.submit(player, "pick_apricorn", event.apricorn.name(), 1);
    };
    /**
     * 精灵升级
     */
    public static final Consumer<LevelUpEvent> LEVEL_UP = (event) -> {
        if (event.player != null) {
            Player player = Bukkit.getPlayer(event.player.getUniqueID());
            BingoApi.submit(player, "pixelmon_level_up", event.pokemon.getSpecies().name(), 1);
        }
    };
    /**
     * 玩家交换精灵
     */
    public static final Consumer<PixelmonTradeEvent> TRADE_POKEMON = (event) -> {
        Player player1 = Bukkit.getPlayer(event.player1.getUniqueID()), player2 = Bukkit.getPlayer(event.player2.getUniqueID());
        BingoApi.submit(player1, "trade_pokemon", event.pokemon1.getSpecies().name(), 1);
        BingoApi.submit(player2, "trade_pokemon", event.pokemon2.getSpecies().name(), 1);
    };
    /**
     * 设置精灵名
     */
    public static final Consumer<SetNicknameEvent> SET_NICK_NAME = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUniqueID());
        BingoApi.submit(player, "set_nick_name", event.pokemon.getSpecies().name(), 1);
    };
    /**
     * 玩家精灵 EVs 增加
     */
    public static final Consumer<EVsGainedEvent> EVS_GAINED = (event) -> {
        if (event.pokemon.getOwnerPlayer() != null) {
            Player player = Bukkit.getPlayer(event.pokemon.getOwnerPlayer().getUniqueID());
            BingoApi.submit(player, "evs_gained", event.pokemon.getSpecies().name(), Arrays.stream(event.evs).sum());
        }
    };
    /**
     * 玩家精灵经验增加
     */
    public static final Consumer<ExperienceGainEvent> EXPERIENCE_GAIN = (event) -> {
        if (event.pokemon.getPokemon().getOwnerPlayer() != null) {
            if (event.pokemon.getPokemon().getSpecies() == null) {
                return;
            }
            Player player = Bukkit.getPlayer(event.pokemon.getPokemon().getOwnerPlayerUUID());
            BingoApi.submit(player, "experience_gain", event.pokemon.getPokemon().getSpecies().name(), event.getExperience());
        }
    };
    /**
     * 进化后事件
     */
    public static final Consumer<EvolveEvent.PostEvolve> EVOLVE_POST = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUniqueID());
        if (event.pokemon == null) {
            return;
        }
        BingoApi.submit(player, "poke_post_evolve", event.pokemon.getSpecies().name(), 1);
    };
    /**
     * 跟商人购买物品
     */
    public static final Consumer<ShopkeeperEvent.Purchase> SHOPKEEPER_PURCHASE = (event) -> {
        Player player = Bukkit.getPlayer(event.getEntityPlayer().getUniqueID());
        BingoApi.submit(player, "shopkeeper_purchase", "*", 1);
    };
    /**
     * 跟商人购买物品
     */
    public static final Consumer<ShopkeeperEvent.Sell> SHOPKEEPER_SELL = (event) -> {
        Player player = Bukkit.getPlayer(event.getEntityPlayer().getUniqueID());
        BingoApi.submit(player, "shopkeeper_sell", "*", 1);
    };
    /**
     * 激活祭坛
     */
    public static final Consumer<PlayerActivateShrineEvent> ACTIVATE_SHRINE = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUniqueID());
        BingoApi.submit(player, "activate_shrine", event.shrineType.name(), 1);
    };
    /**
     * 玩家完成任务时
     */
    public static final Consumer<FinishQuestEvent.Complete> QUEST_COMPLETE = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUniqueID());
        BingoApi.submit(player, "quest_complete", event.progress.getQuest().getIdentityName(), 1);
    };
    /**
     * 玩家繁殖出精灵蛋
     */
    public static final Consumer<BreedEvent.MakeEgg> MAKE_EGG = (event) -> {
        Player player = Bukkit.getPlayer(event.owner);
        BingoApi.submit(player, "breed_make_egg", event.getEgg().getSpecies().name, 1);
    };
    /**
     * 玩家孵化一颗蛋
     */
    public static final Consumer<EggHatchEvent.Post> EGG_HATCH = (event) -> {
        Player player = Bukkit.getPlayer(event.getPlayer().getUniqueID());
        BingoApi.submit(player, "egg_hatch", event.getPokemon().getSpecies().name, 1);
    };
}