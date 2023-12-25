package com.aiyostudio.bingo.model.pixelmon.nat.listen.container;

import com.aiyostudio.bingo.api.BingoApi;
import com.pixelmonmod.pixelmon.api.events.*;
import com.pixelmonmod.pixelmon.api.events.pokemon.EVsGainedEvent;
import com.pixelmonmod.pixelmon.api.events.pokemon.SetNicknameEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-23
 */
public class EventConsumerContainer {
    /**
     * 玩家击败野生精灵
     */
    public static final Consumer<BeatWildPixelmonEvent> BEAT_WILD_PIXELMON = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUUID());
        BingoApi.submit(player, "beat_wild_pixelmon", event.wpp.allPokemon[0].pokemon.getSpecies().getName(), 1);
    };
    /**
     * 捕捉野外精灵
     */
    public static final Consumer<CaptureEvent.SuccessfulCapture> CAPTURE_PIXELMON = (event) -> {
        Player player = Bukkit.getPlayer(event.getPlayer().getUUID());
        BingoApi.submit(player, "capture_pixelmon", event.getPokemon().getSpecies().getName(), 1);
    };
    /**
     * 捕捉巢穴精灵
     */
    public static final Consumer<CaptureEvent.SuccessfulRaidCapture> CAPTURE_RAID_PIXELMON = (event) -> {
        Player player = Bukkit.getPlayer(event.getPlayer().getUUID());
        BingoApi.submit(player, "capture_raid_pixelmon", event.getRaidPokemon().getSpecies().getName(), 1);
    };
    /**
     * 击败训练师
     */
    public static final Consumer<BeatTrainerEvent> BEAT_TRAINER = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUUID());
        BingoApi.submit(player, "beat_trainer", event.trainer.getName().getString(), 1);
    };
    /**
     * 钓鱼
     */
    public static final Consumer<FishingEvent.Catch> FISHING_CATCH = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUUID());
        BingoApi.submit(player, "fishing_catch", event.plannedSpawn.getOrCreateEntity().getName().getString(), 1);
    };
    /**
     * 采摘树果
     */
    public static final Consumer<ApricornEvent.Pick> PICK_APRICORN = (event) -> {
        Player player = Bukkit.getPlayer(event.getPlayer().getUUID());
        BingoApi.submit(player, "pick_apricorn", event.getApricorn().name(), 1);
    };
    /**
     * 精灵升级
     */
    public static final Consumer<LevelUpEvent> LEVEL_UP = (event) -> {
        if (event.getPlayer() != null) {
            Player player = Bukkit.getPlayer(event.getPlayer().getUUID());
            BingoApi.submit(player, "pixelmon_level_up", event.getPokemon().getSpecies().getName(), 1);
        }
    };
    /**
     * 玩家交易精灵
     */
    public static final Consumer<PixelmonTradeEvent> TRADE_POKEMON = (event) -> {
        Player player1 = Bukkit.getPlayer(event.getPlayer1().getUUID()), player2 = Bukkit.getPlayer(event.getPlayer2().getUUID());
        BingoApi.submit(player1, "trade_pokemon", event.getPokemon1().getSpecies().getName(), 1);
        BingoApi.submit(player2, "trade_pokemon", event.getPokemon2().getSpecies().getName(), 1);
    };
    /**
     * 设置精灵名
     */
    public static final Consumer<SetNicknameEvent> SET_NICK_NAME = (event) -> {
        Player player = Bukkit.getPlayer(event.player.getUUID());
        BingoApi.submit(player, "set_nick_name", event.pokemon.getSpecies().getName(), 1);
    };
    /**
     * 玩家精灵 EVs 增加
     */
    public static final Consumer<EVsGainedEvent> EVS_GAINED = (event) -> {
        if (event.pokemon.getOwnerPlayer() != null) {
            Player player = Bukkit.getPlayer(event.pokemon.getOwnerPlayer().getUUID());
            BingoApi.submit(player, "evs_gained", event.pokemon.getSpecies().getName(), Arrays.stream(event.evYields.toArray()).sum());
        }
    };
    /**
     * 玩家精灵经验增加
     */
    public static final Consumer<ExperienceGainEvent> EXPERIENCE_GAIN = (event) -> {
        if (event.pokemon.getPokemon().getOwnerPlayer() != null) {
            Player player = Bukkit.getPlayer(event.pokemon.getPokemon().getOwnerPlayerUUID());
            BingoApi.submit(player, "experience_gain", event.pokemon.getSpecies().getName(), event.getExperience());
        }
    };
    /**
     * 进化后事件
     */
    public static final Consumer<EvolveEvent.Post> EVOLVE_POST = (event) -> {
        Player player = Bukkit.getPlayer(event.getPlayer().getUUID());
        if (event.getEntity() == null) {
            return;
        }
        BingoApi.submit(player, "poke_post_evolve", event.getPokemon().getSpecies().getName(), 1);
    };
    /**
     * 跟商人购买物品
     */
    public static final Consumer<ShopkeeperEvent.Purchase> SHOPKEEPER_PURCHASE = (event) -> {
        Player player = Bukkit.getPlayer(event.getEntityPlayer().getUUID());
        BingoApi.submit(player, "shopkeeper_purchase", "none", 1);
    };
    /**
     * 跟商人购买物品
     */
    public static final Consumer<ShopkeeperEvent.Sell> SHOPKEEPER_SELL = (event) -> {
        Player player = Bukkit.getPlayer(event.getEntityPlayer().getUUID());
        BingoApi.submit(player, "shopkeeper_sell", "none", 1);
    };
    /**
     * 跟商人购买物品
     */
    public static final Consumer<PlayerActivateShrineEvent.Post> ACTIVATE_SHRINE = (event) -> {
        Player player = Bukkit.getPlayer(event.getPlayer().getUUID());
        BingoApi.submit(player, "ACTIVATE_SHRINE", event.getShrineType().name(), 1);
    };
}