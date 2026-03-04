package xyz.pyxismc.manhunt.Listeners.Events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import xyz.pyxismc.manhunt.GUIs.ManhuntGUI;

import java.time.Duration;

import java.util.*;

public class StartGameListener implements Listener {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final JavaPlugin plugin;
    private final ManhuntGUI manhuntGUI;

    private int configValue = 600; // Default 30 seconds
    private final Set<UUID> frozenPlayers = new HashSet<>();

    private long gameStartTime;
    private BukkitTask timerTask;
    private boolean isGameActive = false;

    public StartGameListener(JavaPlugin plugin, ManhuntGUI manhuntGUI) {
        this.plugin = plugin;
        this.manhuntGUI = manhuntGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        if (title.contains("Manhunt configuration")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.CLOCK) {
                ClickType clickType = event.getClick();

                // Adjust by 10 seconds (200 ticks)
                if (clickType.isLeftClick()) {
                    configValue += 200;
                } else if (clickType.isRightClick()) {
                    configValue = Math.max(0, configValue - 200);
                }

                player.playSound(player.getLocation(), "minecraft:ui.button.click", 1.0f, 1.0f);
                // Important: Call the refresh method we fixed in ManhuntGUI
                manhuntGUI.refreshConfigMenu(event.getInventory());
            }
        }

        else if (title.contains("Manhunt Menu")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.LIME_CANDLE) {
                player.closeInventory();
                startGame(player);
            }
        }
    }

    private void startGame(Player starter) {
        World world = Bukkit.getWorld("world");
        if (world == null) {
            starter.sendMessage(miniMessage.deserialize("<red>Error: World 'world' not found!"));
            return;
        }


        ItemStack compass = new ItemStack(Material.COMPASS);
        compass.editMeta(meta -> {

            meta.displayName(miniMessage.deserialize("<!italic><#d60f0f>Compass Tracker"));

            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<!italic><dark_gray>» <gray>Left-Click to <#d60f0f>update location"));
            lore.add(miniMessage.deserialize("<!italic><dark_gray>» <gray>Right-Click to <#d60f0f>switch runner"));
            meta.lore(lore);
        });

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.getInventory().clear();
            if (onlinePlayer.hasPermission("manhunt.hunter")) {
                onlinePlayer.getInventory().addItem(compass);
            }
        }

        Location spawnLocation = world.getSpawnLocation();
        Component startMessage = miniMessage.deserialize("<newline><#e61717>⚔ <dark_gray>» <#e61717>Manhunt Game started!<newline>");
        Bukkit.broadcast(startMessage);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.teleport(spawnLocation);

            double maxHealth = onlinePlayer.getAttribute(Attribute.MAX_HEALTH).getValue();
            onlinePlayer.setHealth(maxHealth);
            onlinePlayer.setFoodLevel(20);
            onlinePlayer.setSaturation(12.5f);

            onlinePlayer.playSound(onlinePlayer.getLocation(), "minecraft:entity.player.levelup", 1.0f, 1.0f);

            if (!onlinePlayer.hasPermission("manhunt.hunter") && !onlinePlayer.hasPermission("manhunt.runner")) {
                Bukkit.getScheduler().runTaskLater(plugin,
                        () -> onlinePlayer.setGameMode(GameMode.SPECTATOR), 1L);
                onlinePlayer.sendMessage(miniMessage.deserialize("<gray>You are spectating the game!"));
            }

            if (onlinePlayer.hasPermission("manhunt.hunter")) {
                if (configValue == 0) {
                    return;
                } else {
                    frozenPlayers.add(onlinePlayer.getUniqueId());
                    onlinePlayer.sendMessage(miniMessage.deserialize("<#e61717>⚔ <dark_gray>» <#e61717>You're freeze for " + (configValue / 20) + " seconds!"));
                }
            }
        }

        startTimer();
        if (configValue >= 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                frozenPlayers.clear();

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {

                    onlinePlayer.sendMessage(miniMessage.deserialize(" "));
                    onlinePlayer.sendMessage(miniMessage.deserialize("<#e61717>⚔ <dark_gray>» <#e61717>The HeadStart is now over!"));
                    onlinePlayer.sendMessage(miniMessage.deserialize(" "));

                    if (onlinePlayer.hasPermission("manhunt.hunter")) {

                        onlinePlayer.playSound(
                                onlinePlayer.getLocation(),
                                Sound.BLOCK_NOTE_BLOCK_PLING,
                                1.0f,
                                2.0f
                        );

                        onlinePlayer.showTitle(Title.title(
                                miniMessage.deserialize("<#e61717><bold>GO!"),
                                Component.empty(),
                                Title.Times.times(
                                        Duration.ofMillis(250),   // fade in (5 ticks)
                                        Duration.ofMillis(2000),  // stay (40 ticks)
                                        Duration.ofMillis(1000)   // fade out (20 ticks)
                                )
                        ));
                    }
                }
            }, configValue);
        }
    }

    private void startTimer() {
        gameStartTime = System.currentTimeMillis();
        isGameActive = true;

        timerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isGameActive) {
                return;
            }

            long elapsedMillis = System.currentTimeMillis() - gameStartTime;
            String timeString = formatTime(elapsedMillis);

            Component timerDisplay = miniMessage.deserialize("<gray>" + timeString);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendActionBar(timerDisplay);
            }
        }, 0L, 20L); // Run every second (20 ticks)
    }

    public void stopTimer() {
        isGameActive = false;
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    public String getElapsedTime() {
        if (!isGameActive) {
            return "00:00:00";
        }
        long elapsedMillis = System.currentTimeMillis() - gameStartTime;
        return formatTime(elapsedMillis);
    }

    private String formatTime(long millis) {
        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                event.setTo(from.setDirection(to.getDirection()));
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.hasPermission("manhunt.hunter")) {
                        onlinePlayer.sendActionBar(miniMessage.deserialize("<#e61717>⚔ <dark_gray>» <#e61717>You can't move during the head start!"));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player damager) {
            if (frozenPlayers.contains(damager.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (frozenPlayers.contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    public int getConfigValue() { return configValue; }
    public void setConfigValue(int value) { this.configValue = value; }
    public boolean isGameActive() { return isGameActive; }
}