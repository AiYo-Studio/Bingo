package com.aiyostudio.bingo.util;

import com.aiyostudio.bingo.Bingo;
import com.aiyostudio.bingo.config.DefaultConfig;
import com.aiyostudio.bingo.handler.format.Formatter;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.List;

/**
 * @author Blank038
 */
public class ScriptUtil {
    private static ScriptEngine scriptEngine;

    public static void initScriptEngine() {
        String engineName = DefaultConfig.getConfig().getString("script-engine", "JavaScript");
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R1)) {
            try {
                ScriptEngineFactory factory = (ScriptEngineFactory) Class.forName("org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory").newInstance();
                scriptEngineManager.registerEngineName("nashorn", factory);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                Bingo.getInstance().getLogger().warning("not found the Nashorn ScriptEngine");
            }
        }
        if (scriptEngine == null) {
            RegisteredServiceProvider<ScriptEngineManager> provider = Bukkit.getServicesManager().getRegistration(ScriptEngineManager.class);
            if (provider != null && provider.getProvider() != null) {
                scriptEngineManager = provider.getProvider();
            }
        }
        scriptEngine = scriptEngineManager.getEngineByName(engineName);
    }

    public static boolean detectionCondition(Player player, List<String> conditions) {
        if (conditions.isEmpty()) {
            return true;
        }
        if (scriptEngine == null) {
            Bingo.getInstance().getLogger().warning("Cannot invoke 'ScriptUtil.detectionCondition', beacuse 'ScriptUtil.scriptEngine' is null");
            return false;
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
            return (boolean) scriptEngine.eval(Formatter.format(player, stringBuilder.toString()));
        } catch (Exception e) {
            Bingo.getInstance().getLogger().severe(" Condition is invalid " + e);
            return false;
        }
    }
}
