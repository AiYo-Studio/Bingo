package com.aiyostudio.bingo.model.pixelmon.legacy;

import com.aiyostudio.bingo.Bingo;
import com.aiyostudio.bingo.model.pixelmon.legacy.listen.ForgeListener;
import com.aiyostudio.bingo.service.IModelService;
import com.pixelmonmod.pixelmon.Pixelmon;
import org.bukkit.Bukkit;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-23
 */
public class PixelmonLegacyModelServiceImpl implements IModelService {

    @Override
    public void run() {
        try {
            if (Pixelmon.getVersion().startsWith("8")) {
                Bukkit.getPluginManager().registerEvents(new ForgeListener(), Bingo.getInstance());
                Bingo.getInstance().getConsoleLogger().log(false, "&e    + &fPixelmonLegacy: &aON");
            }
        } catch (Exception ignored) {
        }
    }
}
