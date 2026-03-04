package xyz.pyxismc.manhunt.Listeners.Events;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.pyxismc.manhunt.Manhunt;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathListener implements Listener {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final JavaPlugin plugin;
    private final Map<UUID, PermissionAttachment> runnerAttachments = new HashMap<>();

    public DeathListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void grantRunnerPermission(Player player) {
        PermissionAttachment attachment = player.addAttachment(plugin);
        attachment.setPermission("manhunt.runner", true);
        runnerAttachments.put(player.getUniqueId(), attachment);
    }


    public void revokeRunnerPermission(Player player) {
        PermissionAttachment attachment = runnerAttachments.remove(player.getUniqueId());
        if (attachment != null) {
            player.removeAttachment(attachment);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (player.hasPermission("manhunt.hunter")) {
            return;
        }

        if (player.hasPermission("manhunt.runner")) {

            revokeRunnerPermission(player);

            long aliveRunners = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("manhunt.runner"))
                    .filter(p -> p.getGameMode() != GameMode.SPECTATOR)
                    .count();

            if (aliveRunners == 0) {
                announceHunterVictory();
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (runnerAttachments.containsKey(player.getUniqueId())
                || !player.hasPermission("manhunt.runner")) {
            Bukkit.getScheduler().runTaskLater(plugin,
                    () -> player.setGameMode(GameMode.SPECTATOR), 1L);
        }
    }

    private void announceHunterVictory() {
        String elapsedTime = ((Manhunt) plugin).getStartGameListener().getElapsedTime();
        ((Manhunt) plugin).getStartGameListener().stopTimer();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("manhunt.hunter")) {
                Title title = Title.title(
                        miniMessage.deserialize("<gradient:#e64935:#e69935><bold>VICTORY!"),
                        miniMessage.deserialize("<gray>All runners have been eliminated"),
                        Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
                );

                String seed = Bukkit.getWorld("world").getSeed() + "";
                player.showTitle(title);
                player.playSound(player.getLocation(), "minecraft:ui.toast.challenge_complete", 1.0f, 1.0f);
                player.sendMessage(miniMessage.deserialize(" "));
                player.sendMessage(miniMessage.deserialize("<#e61717>⚔ <dark_gray>» <gray>The hunters team has won the Game !"));
                player.sendMessage(miniMessage.deserialize("<#e61717>⚔ <dark_gray>»<gray> World seed <dark_gray>»<#e61717>" + seed));
                player.sendMessage(miniMessage.deserialize("<#e61717>⚔ <dark_gray>»<gray> Game Duration <dark_gray>»<#e61717>" + elapsedTime));
                player.sendMessage(miniMessage.deserialize(" "));
            }
        }
    }
}