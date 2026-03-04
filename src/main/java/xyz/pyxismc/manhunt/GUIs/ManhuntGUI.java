package xyz.pyxismc.manhunt.GUIs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import xyz.pyxismc.manhunt.Commands.ManhuntCommand;
import xyz.pyxismc.manhunt.Listeners.Configuration.Compass.CompassEnableListener;
import xyz.pyxismc.manhunt.Listeners.Configuration.FriendlyFireListener;
import xyz.pyxismc.manhunt.Listeners.Configuration.OverWorldBorderListener;
import xyz.pyxismc.manhunt.Listeners.Configuration.RunnerFriendlyFireListener;
import xyz.pyxismc.manhunt.Listeners.Events.StartGameListener;

import java.util.List;

public class ManhuntGUI implements Listener, CommandExecutor {

    private final JavaPlugin plugin;
    private final ManhuntCommand manhuntCommand;
    private StartGameListener startGameListener;
    private FriendlyFireListener friendlyFireListener;
    private CompassEnableListener compassEnableListener;
    private RunnerFriendlyFireListener runnerFriendlyFireListener;
    private OverWorldBorderListener overWorldBorderListener;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    // Store inventories as fields so they persist across methods
    private Inventory mainMenu;
    private Inventory selectionMenu;
    private Inventory confMenu;

    public ManhuntGUI(JavaPlugin plugin, ManhuntCommand manhuntCommand) {
        this.plugin = plugin;
        this.manhuntCommand = manhuntCommand;
    }

    public void setListeners(StartGameListener startGameListener,
                             FriendlyFireListener friendlyFireListener,
                             CompassEnableListener compassEnableListener,
                             RunnerFriendlyFireListener runnerFriendlyFireListener,
                             OverWorldBorderListener overWorldBorderListener) {
        this.startGameListener = startGameListener;
        this.friendlyFireListener = friendlyFireListener;
        this.compassEnableListener = compassEnableListener;
        this.runnerFriendlyFireListener = runnerFriendlyFireListener;
        this.overWorldBorderListener = overWorldBorderListener;
    }
    private void initializeMenus() {
        // Main Menu setup
        mainMenu = Bukkit.createInventory(null, 27, miniMessage.deserialize("<gradient:#e64935:#e69935>★ Manhunt Menu"));

        mainMenu.setItem(11, createItem(Material.DIAMOND_BOOTS,
                miniMessage.deserialize("<gradient:#0fb800:#39db6c>Runner Selection"),
                miniMessage.deserialize(" "),
                miniMessage.deserialize("<gray>Assign players as runners or hunters")));

        mainMenu.setItem(13, createItem(Material.WRITABLE_BOOK,
                miniMessage.deserialize("<gradient:#e64935:#e69935>Game Settings"),
                miniMessage.deserialize(" "),
                miniMessage.deserialize("<gray>setup the game settings")));

        mainMenu.setItem(15, createItem(Material.LIME_CANDLE,
                miniMessage.deserialize("<gradient:#00ff00:#00cc00>Start"),
                miniMessage.deserialize(" "),
                miniMessage.deserialize("<dark_gray>» <gray>Click to start the Manhunt")));

        fillEmptySlots(mainMenu);

        // Config Menu setup
        confMenu = Bukkit.createInventory(null, 36, miniMessage.deserialize("<gradient:#e64935:#e69935>▶ Manhunt configuration"));
        updateConfigMenuItems(confMenu);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be executed by a player.");
            return true;
        }

        if (!player.hasPermission("manhunt.admin")) {
            player.sendMessage(miniMessage.deserialize("<red>You don't have permission to use this command!"));
            return true;
        }

        initializeMenus(); // Re-init to ensure fresh items
        player.openInventory(mainMenu);
        return true;
    }

    private void updateConfigMenuItems(Inventory inv) {
        // Compass
        String compassStatus = compassEnableListener.isCompassEnabled() ? "<#05ff26>ON" : "<#ff0505>OFF";
        inv.setItem(10, createItem(Material.COMPASS,
                miniMessage.deserialize("<#ffd700>Compass Tracking"),
                miniMessage.deserialize("<white>Toggle compass tracking"),
                miniMessage.deserialize("<gray>currently <dark_gray>» " + compassStatus),
                miniMessage.deserialize(" "),
                miniMessage.deserialize("<dark_gray>» <gray>Click to change")));

        // Head Start
        inv.setItem(11, createItem(Material.CLOCK,
                miniMessage.deserialize("<#00ffff>Head Start"),
                miniMessage.deserialize("<white>Time before hunters can start tracking"),
                miniMessage.deserialize("<gray>current<dark_gray> » <#00ffff>" + (startGameListener.getConfigValue() / 20) + " seconds"),
                miniMessage.deserialize(" "),
                miniMessage.deserialize("<dark_gray>» <gray>left click to add <#0fb800>+10ꜱ"),
                miniMessage.deserialize("<dark_gray>» <gray>right click to remove <#e00909>-10ꜱ")));

        // Friendly Fire Hunters
        String hunterStatus = friendlyFireListener.isFriendlyFireEnabled() ? "<#05ff26>ON" : "<#ff0505>OFF";
        inv.setItem(12, createItem(Material.FIRE_CHARGE,
                miniMessage.deserialize("<#ff0000>Hunters Friendly Fire"),
                miniMessage.deserialize("<white>Allows hunters to damage each other"),
                miniMessage.deserialize("<gray>currently <dark_gray>» " + hunterStatus),
                miniMessage.deserialize(" "),
                miniMessage.deserialize("<dark_gray>» <gray>Click to change")));

        // Friendly Fire Runner
        String runnerStatus = runnerFriendlyFireListener.isFriendlyFireEnabled() ? "<#05ff26>ON" : "<#ff0505>OFF";
        inv.setItem(13, createItem(Material.FIRE_CHARGE,
                miniMessage.deserialize("<#60e319>Runners Friendly Fire"),
                miniMessage.deserialize("<white>Allows runners to damage each other"),
                miniMessage.deserialize("<gray>currently <dark_gray>» " + runnerStatus),
                miniMessage.deserialize(" "),
                miniMessage.deserialize("<dark_gray>» <gray>Click to change")));

        // WorldBorder
        inv.setItem(14, createItem(Material.GRASS_BLOCK,
                miniMessage.deserialize("<#00ff00>OverWorld Border"),
                miniMessage.deserialize("<white>Size of the overworld border"),
                miniMessage.deserialize("<gray>current<dark_gray> » <#00ff00>" + overWorldBorderListener.getWorldBorderSize() + " blocks"),
                miniMessage.deserialize(" "),
                miniMessage.deserialize("<dark_gray>» <gray>left click to add <#0fb800>+200"),
                miniMessage.deserialize("<dark_gray>» <gray>right click to remove <#e00909>-200")));


        inv.setItem(8, createItem(Material.BARRIER, miniMessage.deserialize("<#ff0000>Reset"), miniMessage.deserialize("<dark_gray>» <white>Reset all game settings")));
        inv.setItem(0, createItem(Material.RED_DYE, miniMessage.deserialize("<#e00909>Back"), miniMessage.deserialize("<dark_gray>» <white>return to the last menu")));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Use PlainText for more reliable title checking
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        if (title.contains("Manhunt Menu")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;

            else if (clicked.getType() == Material.WRITABLE_BOOK) {
                updateConfigMenuItems(confMenu);
                player.openInventory(confMenu);
            }
        }

        else if (title.contains("Manhunt configuration")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.LIGHT_GRAY_STAINED_GLASS_PANE) return;

            if (clicked.getType() == Material.RED_DYE) {
                player.openInventory(mainMenu);
            } else if (clicked.getType() == Material.BARRIER) {
                startGameListener.setConfigValue(600);
                friendlyFireListener.setFriendlyFireEnabled(false);
                runnerFriendlyFireListener.setFriendlyFireEnabled(false);
                compassEnableListener.setCompassEnabled(true);
                overWorldBorderListener.setWorldBorderSize(5000);
                updateConfigMenuItems(event.getInventory());
                player.sendMessage(miniMessage.deserialize("<green>Settings Reset!"));
            }
            // Use a small delay for other listeners to process state changes before refreshing
            Bukkit.getScheduler().runTaskLater(plugin, () -> updateConfigMenuItems(event.getInventory()), 1L);
        }
    }

    private void fillEmptySlots(Inventory inv) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.displayName(Component.empty());
        filler.setItemMeta(meta);
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                inv.setItem(i, filler);
            }
        }
    }

    private ItemStack createItem(Material material, Component name, Component... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name.decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(lore).stream().map(l -> l.decoration(TextDecoration.ITALIC, false)).toList());
            item.setItemMeta(meta);
        }
        return item;
    }
    /**
     * Public method to refresh the config menu - called by other listeners
     */
    public void refreshConfigMenu(Inventory inventory) {
        updateConfigMenuItems(inventory);
    }
}