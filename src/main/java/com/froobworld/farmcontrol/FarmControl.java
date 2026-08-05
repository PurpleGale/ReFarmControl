package com.froobworld.farmcontrol;

import com.froobworld.farmcontrol.command.FarmControlCommand;
import com.froobworld.farmcontrol.command.FarmControlCommandRegistrar;
import com.froobworld.farmcontrol.config.FcConfig;
import com.froobworld.farmcontrol.controller.*;
import com.froobworld.farmcontrol.controller.action.RemoveRandomMovementAction;
import com.froobworld.farmcontrol.i18n.LanguageManager;
import com.froobworld.farmcontrol.metrics.FcMetrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class FarmControl extends JavaPlugin {
    private FcConfig fcConfig;
    private LanguageManager languageManager;
    private HookManager hookManager;
    private ActionManager actionManager;
    private TriggerManager triggerManager;
    private ProfileManager profileManager;
    private ExclusionManager exclusionManager;
    private FarmController farmController;
    private SpawnLimiter spawnLimiter;

    public void onEnable() {
        this.fcConfig = new FcConfig(this);
        try {
            fcConfig.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.languageManager = new LanguageManager(this);
        this.languageManager.load();

        hookManager = new HookManager(this);
        hookManager.load();
        actionManager = new ActionManager();
        actionManager.addDefaults(this);
        triggerManager = new TriggerManager();
        triggerManager.addDefaults(this);
        profileManager = new ProfileManager(this);
        try {
            profileManager.load();
        } catch (IOException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        exclusionManager = new ExclusionManager(this);
        spawnLimiter = new SpawnLimiter(this);
        farmController = new FarmController(this);
        farmController.load();
        farmController.register();

        FarmControlCommandRegistrar.register(this);

        new FcMetrics(this, 9692);
        hookManager.getSchedulerHook().runRepeatingTask(() -> RemoveRandomMovementAction.cleanUp(this), 1200, 1200);
    }

    public void reload() throws Exception {
        fcConfig.load();
        languageManager.load();
        hookManager.reload();
        farmController.unRegister();
        profileManager.reload();
        if (spawnLimiter != null) {
            spawnLimiter.clearCache();
        }
        farmController.reload();
        farmController.register();
    }

    public void onDisable() {
        if (farmController != null) {
            farmController.unRegister();
            farmController.unload();
        }
        RemoveRandomMovementAction.cleanUp(this);
    }

    public FcConfig getFcConfig() {
        return fcConfig;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    public TriggerManager getTriggerManager() {
        return triggerManager;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public FarmController getFarmController() {
        return farmController;
    }

    public ExclusionManager getExclusionManager() {
        return exclusionManager;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public void registerCommands() {
        FarmControlCommand farmControlCommand = new FarmControlCommand(this);
        if (getCommand("farmcontrol") != null) {
            getCommand("farmcontrol").setExecutor(farmControlCommand);
            getCommand("farmcontrol").setTabCompleter(farmControlCommand.getTabCompleter());
            getCommand("farmcontrol").setPermission("farmcontrol.command.farmcontrol");
        }
    }
}
