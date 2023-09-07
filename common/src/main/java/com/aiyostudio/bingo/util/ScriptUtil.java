package com.aiyostudio.bingo.util;

import com.aiyostudio.bingo.Bingo;
import com.aiyostudio.bingo.hook.placeholders.PlaceholderHook;
import org.bukkit.entity.Player;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.List;

/**
 * @author Blank038
 */
public class ScriptUtil {
    private static ScriptEngine scriptEngine;

    public static void initScriptEngine() {
        scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
    }

    public static boolean detectionCondition(Player player, List<String> conditions) {
        if (scriptEngine == null) {
            return false;
        }
        if (conditions.isEmpty()) {
            return true;
        }
        try {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < conditions.size(); i++) {
                if (i + 1 == conditions.size()) {
                    stringBuilder.append(conditions.get(i));
                } else {
                    stringBuilder.append(conditions.get(i)).append(" && ");
                }
            }
            return (boolean) scriptEngine.eval(PlaceholderHook.format(player, stringBuilder.toString()));
        } catch (Exception e) {
            Bingo.getInstance().getLogger().severe(" Condition is invalid " + e);
            return false;
        }
    }
}
