package xyz.pyxismc.manhunt;

import org.bukkit.plugin.java.JavaPlugin;

public class Manhunt extends JavaPlugin {

    private ManhuntCommand manhuntCommand;
    private ManhuntGUI manhuntGUI;
    private CompassListener compassListener;
    private StartGameListener startGameListener;
    private RunnerFriendlyFireListener runnerFriendlyFireListener;
    private OverWorldBorderListener overWorldBorderListener;

    @Override
    public void onEnable() {
        // Initialize manhuntCommand first
        manhuntCommand = new ManhuntCommand(this);

        // Initialize GUI early (without listeners yet)
        manhuntGUI = new ManhuntGUI(this, manhuntCommand);

        // Now initialize listeners WITH manhuntGUI reference
        startGameListener = new StartGameListener(this, manhuntGUI);
        FriendlyFireListener friendlyFireListener = new FriendlyFireListener(manhuntGUI);
        CompassEnableListener compassEnableListener = new CompassEnableListener(manhuntGUI);
        runnerFriendlyFireListener = new RunnerFriendlyFireListener(manhuntGUI);
        overWorldBorderListener = new OverWorldBorderListener(manhuntGUI);


        // Set the listeners in manhuntGUI
        // In Manhunt.java, update this line:
        manhuntGUI.setListeners(startGameListener, friendlyFireListener, compassEnableListener, runnerFriendlyFireListener, overWorldBorderListener);

        // Initialize compass listener
        compassListener = new CompassListener(this, compassEnableListener);

        // Register event listeners
        getServer().getPluginManager().registerEvents(startGameListener, this);
        getServer().getPluginManager().registerEvents(friendlyFireListener, this);
        getServer().getPluginManager().registerEvents(compassEnableListener, this);
        getServer().getPluginManager().registerEvents(runnerFriendlyFireListener, this);
        getServer().getPluginManager().registerEvents(compassListener, this);
        getServer().getPluginManager().registerEvents(manhuntGUI, this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new RunnerSelectionListener(this ), this);
        getServer().getPluginManager().registerEvents(overWorldBorderListener, this);

        // Register commands
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

        // Success message
        getLogger().info("========================================");
        getLogger().info("  Manhunt Plugin Enabled!");
        getLogger().info("  Version: " + getDescription().getVersion());
        getLogger().info("  Commands: /manhunt, /runner");
        getLogger().info("========================================");
    }

    @Override
    public void onDisable() {
        // Clean up permissions
        if (manhuntCommand != null) {
            manhuntCommand.cleanup();
        }

        getLogger().info("========================================");
        getLogger().info("  Manhunt Plugin Disabled!");
        getLogger().info("  All configurations saved.");
        getLogger().info("========================================");
    }

    /**
     * Get the manhunt command handler
     * @return ManhuntCommand instance
     */
    public ManhuntCommand getManhuntCommand() {
        return manhuntCommand;
    }

    /**
     * Get the GUI handler
     * @return ManhuntGUI instance
     */
    public ManhuntGUI getManhuntGUI() {
        return manhuntGUI;
    }

    /**
     * Get the compass listener
     * @return CompassListener instance
     */
    public CompassListener getCompassListener() {
        return compassListener;
    }

    /**
     * Get the start game listener
     * @return StartGameListener instance
     */
    public StartGameListener getStartGameListener() {
        return startGameListener;
    }

    /**
     * Get the runner friendly fire listener
     * @return RunnerFriendlyFireListener instance
     */
    public RunnerFriendlyFireListener getRunnerFriendlyFireListener() {
        return runnerFriendlyFireListener;
    }

    public OverWorldBorderListener getOverWorldBorderListener() {
        return overWorldBorderListener;
    }
}