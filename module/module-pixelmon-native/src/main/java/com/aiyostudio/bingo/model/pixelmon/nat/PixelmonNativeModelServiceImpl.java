package com.aiyostudio.bingo.model.pixelmon.nat;

import com.aiyostudio.bingo.Bingo;
import com.aiyostudio.bingo.model.pixelmon.nat.listen.ForgeListener;
import com.aiyostudio.bingo.service.IModelService;
import com.pixelmonmod.pixelmon.Pixelmon;
import org.bukkit.Bukkit;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-23
 */
public class PixelmonNativeModelServiceImpl implements IModelService {

    @Override
    public void run() {
        try {
            if (Pixelmon.getVersion().startsWith("9")) {
                Bukkit.getPluginManager().registerEvents(new ForgeListener(), Bingo.getInstance());
                Bingo.getInstance().getConsoleLogger().log(false, "&e    + &fPixelmonNative: &aON");
            }
        } catch (Exception ignored) {
        }
    }
}
