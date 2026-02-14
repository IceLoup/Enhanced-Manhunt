package xyz.pyxismc.manhunt;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;

public class DeathListener implements Listener {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final JavaPlugin plugin;

    public DeathListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // If hunter dies, do nothing
        if (player.hasPermission("manhunt.hunter")) {
            return;
        }

        // If runner dies, check if there are other runners alive
        if (player.hasPermission("manhunt.runner")) {
            // Check if there are other alive runners
            long aliveRunners = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("manhunt.runner"))
                    .filter(p -> !p.equals(player)) // Exclude the dead player
                    .filter(p -> p.getGameMode() != GameMode.SPECTATOR) // Exclude spectators
                    .count();

            // If no other runners are alive, hunters win
            if (aliveRunners == 0) {
                announceHunterVictory();
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // If the player is a runner, put them in spectator mode
        if (player.hasPermission("manhunt.runner")) {
            // Use a small delay to ensure the player has respawned properly
            Bukkit.getScheduler().runTaskLater(plugin,
                    () -> player.setGameMode(GameMode.SPECTATOR), 1L);
        }
    }

    private void announceHunterVictory() {
        // Stop the timer and get elapsed time
        String elapsedTime = ((Manhunt) plugin).getStartGameListener().getElapsedTime();
        ((Manhunt) plugin).getStartGameListener().stopTimer();

        // Send title and sound to all hunters
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("manhunt.hunter")) {
                // Send title
                Title title = Title.title(
                        miniMessage.deserialize("<gradient:#e64935:#e69935><bold>VICTORY!"),
                        miniMessage.deserialize("<gray>All runners have been eliminated"),
                        Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
                );
                player.showTitle(title);

                // Play victory sound
                player.playSound(player.getLocation(), "minecraft:ui.toast.challenge_complete", 1.0f, 1.0f);

                // Send time message
                player.sendMessage(miniMessage.deserialize(":manhunt: <gray>Game Duration: <#ffd700>" + elapsedTime));
            }
        }
    }
}