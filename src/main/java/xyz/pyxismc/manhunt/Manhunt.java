package xyz.pyxismc.manhunt;

import org.bukkit.plugin.java.JavaPlugin;
import xyz.pyxismc.manhunt.Commands.ManhuntCommand;
import xyz.pyxismc.manhunt.GUIs.ManhuntGUI;
import xyz.pyxismc.manhunt.GUIs.SelectionGUI;
import xyz.pyxismc.manhunt.Listeners.Configuration.*;
import xyz.pyxismc.manhunt.Listeners.Configuration.Compass.CompassEnableListener;
import xyz.pyxismc.manhunt.Listeners.Configuration.Compass.CompassListener;
import xyz.pyxismc.manhunt.Listeners.Events.*;

public class Manhunt extends JavaPlugin {

    private ManhuntCommand manhuntCommand;
    private ManhuntGUI manhuntGUI;
    private CompassListener compassListener;
    private StartGameListener startGameListener;
    private RunnerFriendlyFireListener runnerFriendlyFireListener;
    private OverWorldBorderListener overWorldBorderListener;

    @Override
    public void onEnable() {
        manhuntCommand = new ManhuntCommand(this);

        manhuntGUI = new ManhuntGUI(this, manhuntCommand);

        startGameListener = new StartGameListener(this, manhuntGUI);
        FriendlyFireListener friendlyFireListener = new FriendlyFireListener(manhuntGUI);
        CompassEnableListener compassEnableListener = new CompassEnableListener(manhuntGUI);
        runnerFriendlyFireListener = new RunnerFriendlyFireListener(manhuntGUI);
        overWorldBorderListener = new OverWorldBorderListener(manhuntGUI);

        manhuntGUI.setListeners(startGameListener, friendlyFireListener, compassEnableListener, runnerFriendlyFireListener, overWorldBorderListener);

        compassListener = new CompassListener(this, compassEnableListener);

        getServer().getPluginManager().registerEvents(startGameListener, this);
        getServer().getPluginManager().registerEvents(friendlyFireListener, this);
        getServer().getPluginManager().registerEvents(compassEnableListener, this);
        getServer().getPluginManager().registerEvents(runnerFriendlyFireListener, this);
        getServer().getPluginManager().registerEvents(compassListener, this);
        getServer().getPluginManager().registerEvents(manhuntGUI, this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(overWorldBorderListener, this);
        getServer().getPluginManager().registerEvents(new WinListener(), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new RespawnListener(this), this);
        getServer().getPluginManager().registerEvents(new SelectionGUI(this), this);

        if (getCommand("runner") != null) {
            getCommand("runner").setExecutor(manhuntCommand);
        } else {
            getLogger().warning("Command 'runner' not found in plugin.yml!");
        }

        if (getCommand("manhunt") != null) {
            getCommand("manhunt").setExecutor(manhuntGUI);
        } else {
            getLogger().warning("Command 'manhunt' not found in plugin.yml!");
        }

        getLogger().info("========================================");
        getLogger().info("  Manhunt Plugin Enabled!");
        getLogger().info("  Version: " + getDescription().getVersion());
        getLogger().info("  Commands: /manhunt, /runner");
        getLogger().info("========================================");
    }

    @Override
    public void onDisable() {
        if (manhuntCommand != null) {
            manhuntCommand.cleanup();
        }

        getLogger().info("========================================");
        getLogger().info("  Manhunt Plugin Disabled!");
        getLogger().info("  All configurations saved.");
        getLogger().info("========================================");
    }

    public ManhuntCommand getManhuntCommand() {
        return manhuntCommand;
    }

    public ManhuntGUI getManhuntGUI() {
        return manhuntGUI;
    }

    public CompassListener getCompassListener() {
        return compassListener;
    }

    public StartGameListener getStartGameListener() {
        return startGameListener;
    }

    public RunnerFriendlyFireListener getRunnerFriendlyFireListener() {
        return runnerFriendlyFireListener;
    }

    public OverWorldBorderListener getOverWorldBorderListener() {
        return overWorldBorderListener;
    }
}