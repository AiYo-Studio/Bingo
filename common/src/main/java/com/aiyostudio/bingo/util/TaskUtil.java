package com.aiyostudio.bingo.util;

import com.aiyostudio.bingo.Bingo;
import org.bukkit.Bukkit;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
public class TaskUtil {

    public static void runAsyncTask(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(Bingo.getInstance(), runnable);
    }
}
