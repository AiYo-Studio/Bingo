package com.aiyostudio.bingo.model.pixelmon.legacy.listen.container;

import com.aiyostudio.bingo.api.BingoApi;
import com.aiyostudio.bingo.api.interfaces.EventExecutor;
import com.pixelmonmod.pixelmon.api.events.*;
import com.pixelmonmod.pixelmon.api.events.pokemon.EVsGainedEvent;
import com.pixelmonmod.pixelmon.api.events.pokemon.SetNicknameEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-23
 */
public class ForgeEventExecutorContainer {
    /**
     * 玩家击败野生精灵
     */
    public static final EventExecutor<BeatWildPixelmonEvent> BEAT_WILD_PIXELMON = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUniqueID());
        BingoApi.submit(player, "beat_wild_pixelmon", event.wpp.allPokemon[0].pokemon.getSpecies().name(), 1);
    };
    /**
     * 捕捉野外精灵
     */
    public static final EventExecutor<CaptureEvent.SuccessfulCapture> CAPTURE_PIXELMON = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUniqueID());
        BingoApi.submit(player, "capture_pixelmon", event.getPokemon().getSpecies().name(), 1);
    };
    /**
     * 捕捉巢穴精灵
     */
    public static final EventExecutor<CaptureEvent.SuccessfulRaidCapture> CAPTURE_RAID_PIXELMON = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUniqueID());
        BingoApi.submit(player, "capture_raid_pixelmon", event.getRaidPokemon().getSpecies().name(), 1);
    };
    /**
     * 击败训练师
     */
    public static final EventExecutor<BeatTrainerEvent> BEAT_TRAINER = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUniqueID());
        BingoApi.submit(player, "beat_trainer", event.trainer.getName(), 1);
    };
    /**
     * 钓鱼
     */
    public static final EventExecutor<FishingEvent.Catch> FISHING_CATCH = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUniqueID());
        BingoApi.submit(player, "fishing_catch", event.plannedSpawn.getOrCreateEntity().getName(), 1);
    };
    /**
     * 采摘树果
     */
    public static final EventExecutor<ApricornEvent.PickApricorn> PICK_APRICORN = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUniqueID());
        BingoApi.submit(player, "pick_apricorn", event.apricorn.name(), 1);
    };
    /**
     * 精灵升级
     */
    public static final EventExecutor<LevelUpEvent> LEVEL_UP = (event) -> {
        if (event.player != null) {
            Player player = Bukkit.getPlayer(event.player.getUniqueID());
            BingoApi.submit(player, "pixelmon_level_up", event.pokemon.getSpecies().name(), 1);
        }
    };
    /**
     * 玩家交换精灵
     */
    public static final EventExecutor<PixelmonTradeEvent> TRADE_POKEMON = (event) -> {
        Player player1 = Bukkit.getPlayer(event.player1.getUniqueID()), player2 = Bukkit.getPlayer(event.player2.getUniqueID());
        BingoApi.submit(player1, "trade_pokemon", event.pokemon1.getSpecies().name(), 1);
        BingoApi.submit(player2, "trade_pokemon", event.pokemon2.getSpecies().name(), 1);
    };
    /**
     * 设置精灵名
     */
    public static final EventExecutor<SetNicknameEvent> SET_NICK_NAME = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUniqueID());
        BingoApi.submit(player, "set_nick_name", event.pokemon.getSpecies().name(), 1);
    };
    /**
     * 玩家精灵 EVs 增加
     */
    public static final EventExecutor<EVsGainedEvent> EVS_GAINED = (event) -> {
        if (event.pokemon.getOwnerPlayer() != null) {
            Player player = Bukkit.getPlayer(event.pokemon.getOwnerPlayer().getUniqueID());
            BingoApi.submit(player, "evs_gained", event.pokemon.getSpecies().name(), Arrays.stream(event.evs).sum());
        }
    };
    /**
     * 玩家精灵经验增加
     */
    public static final EventExecutor<ExperienceGainEvent> EXPERIENCE_GAIN = (event) -> {
        if (event.pokemon.getPokemon().getOwnerPlayer() != null) {
            Player player = Bukkit.getPlayer(event.pokemon.getPokemon().getOwnerPlayerUUID());
            BingoApi.submit(player, "experience_gain", event.pokemon.getSpecies().name(), event.getExperience());
        }
    };
    /**
     * 进化后事件
     */
    public static final EventExecutor<EvolveEvent.PostEvolve> EVOLVE_POST = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUniqueID());
        BingoApi.submit(player, "poke_post_evolve", event.pokemon.getSpecies().name(), 1);
    };
    /**
     * 跟商人购买物品
     */
    public static final EventExecutor<ShopkeeperEvent.Purchase> SHOPKEEPER_PURCHASE = (event) -> {
        Player player = Bukkit.getPlayer(event.getEntityPlayer().getUniqueID());
        BingoApi.submit(player, "shopkeeper_purchase", "*", 1);
    };
    /**
     * 跟商人购买物品
     */
    public static final EventExecutor<ShopkeeperEvent.Sell> SHOPKEEPER_SELL = (event) -> {
        Player player = Bukkit.getPlayer(event.getEntityPlayer().getUniqueID());
        BingoApi.submit(player, "shopkeeper_sell", "*", 1);
    };
    /**
     * 跟商人购买物品
     */
    public static final EventExecutor<PlayerActivateShrineEvent> ACTIVATE_SHRINE = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUniqueID());
        BingoApi.submit(player, "activate_shrine", event.shrineType.name(), 1);
    };
}