package com.aiyostudio.bingo.command;

import com.aiyostudio.bingo.Bingo;
import com.aiyostudio.bingo.cacheframework.cache.ViewCache;
import com.aiyostudio.bingo.cacheframework.manager.CacheManager;
import com.aiyostudio.bingo.i18n.I18n;
import com.aiyostudio.bingo.view.AbstractView;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
public class BingoCommand implements CommandExecutor {
    private final Bingo plugin = Bingo.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            this.help(sender, label);
        } else {
            switch (args[0]) {
                case "view":
                    this.view(sender, args);
                    break;
                case "reload":
                    this.reload(sender);
                    break;
                default:
                    break;
            }
        }
        return false;
    }

    private void help(CommandSender sender, String label) {
        String optionKey = "help." + (sender.hasPermission("psbindbox.admin") ? "admin" : "default");
        for (String line : I18n.getArrayOption(optionKey)) {
            sender.sendMessage(line.replace("%c", label));
        }
    }

    private void view(CommandSender sender, String[] args) {
        if (sender instanceof Player && args.length > 1) {
            Player player = (Player) sender;
            if (!CacheManager.hasViewCache(args[1])) {
                player.sendMessage(I18n.getStrAndHeader("view-not-found"));
                return;
            }
            if (!CacheManager.hasPlayerCache(player.getUniqueId())) {
                player.sendMessage(I18n.getStrAndHeader("data-not-load"));
                return;
            }
            ViewCache viewCache = CacheManager.getViewCache(args[1]);
            if (viewCache == null) {
                return;
            }
            AbstractView.create(player, viewCache).open();
        }
    }

    private void reload(CommandSender sender) {
        if (sender.hasPermission("bingo.admin")) {
            this.plugin.loadConfig();
            CacheManager.getAllPlayerCaches().forEach((k, v) -> v.checkUnlockGroups());
            sender.sendMessage(I18n.getStrAndHeader("reload"));
        }
    }
}
