package com.aiyostudio.bingo.model.pixelmon.legacy.listen;

import com.aiyostudio.bingo.Bingo;
import com.aiyostudio.bingo.api.interfaces.EventExecutor;
import com.aiyostudio.bingo.model.pixelmon.legacy.listen.container.ForgeEventExecutorContainer;
import com.aystudio.core.forge.ForgeInject;
import com.aystudio.core.forge.IForgeListenHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-23
 */
public class ForgeListener implements Listener {
    private final Map<Object, EventExecutor> eventExecutorMap = new HashMap<>();

    public ForgeListener() {
        ForgeInject.getInstance().getForgeListener().registerListener(Bingo.getInstance(), this, EventPriority.NORMAL);
        try {
            Class<ForgeEventExecutorContainer> c = ForgeEventExecutorContainer.class;
            for (Field field : c.getFields()) {
                Type t = field.getGenericType();
                if (t instanceof ParameterizedType) {
                    eventExecutorMap.put(((ParameterizedType) t).getActualTypeArguments()[0], (EventExecutor) field.get(null));
                }
            }
        } catch (IllegalAccessException e) {
            Bingo.getInstance().getLogger().severe(e.toString());
        }
    }

    @IForgeListenHandler.SubscribeEvent
    public void onForge(Event event) {
        if (this.eventExecutorMap.containsKey(event.getClass())) {
            this.eventExecutorMap.get(event.getClass()).run(event);
        }
    }
}
