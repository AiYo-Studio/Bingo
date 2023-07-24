package com.aiyostudio.bingo.model.pixelmon.nat.listen;

import com.aiyostudio.bingo.Bingo;
import com.aiyostudio.bingo.model.pixelmon.nat.listen.container.EventConsumerContainer;
import com.pixelmonmod.pixelmon.Pixelmon;
import net.minecraftforge.eventbus.api.EventPriority;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Consumer;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-23
 */
public class ForgeListener {

    public static void init() {
        try {
            Class<EventConsumerContainer> c = EventConsumerContainer.class;
            for (Field field : c.getFields()) {
                Type t = field.getGenericType();
                if (t instanceof ParameterizedType) {
                    Pixelmon.EVENT_BUS.addListener(EventPriority.HIGHEST, false,
                            (Class) ((ParameterizedType) t).getActualTypeArguments()[0], (Consumer) field.get(null));
                }
            }
        } catch (IllegalAccessException e) {
            Bingo.getInstance().getLogger().severe(e.toString());
        }
    }
}
