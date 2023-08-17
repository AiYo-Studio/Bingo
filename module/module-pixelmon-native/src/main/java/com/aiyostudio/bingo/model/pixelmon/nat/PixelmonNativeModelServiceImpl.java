package com.aiyostudio.bingo.model.pixelmon.nat;

import com.aiyostudio.bingo.Bingo;
import com.aiyostudio.bingo.model.pixelmon.nat.listen.ForgeListener;
import com.aiyostudio.bingo.service.IModelService;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-23
 */
public class PixelmonNativeModelServiceImpl implements IModelService {

    @Override
    public void run() {
        if (com.pixelmonmod.pixelmon.Pixelmon.getVersion().startsWith("9")) {
            ForgeListener.init();
            Bingo.getInstance().getConsoleLogger().log(false, "&e    + &fPixelmonNative: &aON");
        }
    }
}
