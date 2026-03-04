package xyz.pyxismc.manhunt.Listeners.Events;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import xyz.pyxismc.manhunt.Manhunt;

import java.time.Duration;

public class WinListener implements Listener {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) return;

        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        if (killer.hasPermission("manhunt.runner")) {
            announceRunnerVictory(killer);
        }
    }

    private void announceRunnerVictory(Player killer) {
        World world = Bukkit.getWorld("world");
        long seed = world != null ? world.getSeed() : 0;
        Manhunt manhuntPlugin = (Manhunt) Bukkit.getPluginManager().getPlugin("Manhunt");
        String elapsedTime = manhuntPlugin.getStartGameListener().getElapsedTime();
        manhuntPlugin.getStartGameListener().stopTimer();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("manhunt.runner")) {
                Title title = Title.title(
                        miniMessage.deserialize("<gradient:#0fb800:#39db6c><bold>VICTORY!"),
                        miniMessage.deserialize(""),
                        Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(4), Duration.ofMillis(500))
                );
                player.showTitle(title);

                player.playSound(player.getLocation(), "minecraft:ui.toast.challenge_complete", 1.0f, 1.0f);

                player.sendMessage(miniMessage.deserialize(" "));
                player.sendMessage(miniMessage.deserialize("<#e61717>⚔ <dark_gray>» <gray>The runners team has won the Game <#e61717>" + killer.getName() + " <gray>has defeated the Ender Dragon!"));
                player.sendMessage(miniMessage.deserialize("<#e61717>⚔ <dark_gray>»<gray> World seed <dark_gray>»<#e61717>" + seed));
                player.sendMessage(miniMessage.deserialize("<#e61717>⚔ <dark_gray>»<gray> Game Duration <dark_gray>»<#e61717>" + elapsedTime));
                player.sendMessage(miniMessage.deserialize(" "));
            }
            else if (player.hasPermission("manhunt.hunter")) {
                Title title = Title.title(
                        miniMessage.deserialize("<#e61717><bold>DEFEAT!"),
                        miniMessage.deserialize(""),
                        Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(4), Duration.ofMillis(500))
                );
                player.showTitle(title);

                player.playSound(player.getLocation(), "minecraft:entity.wither.death", 0.5f, 0.8f);

                player.sendMessage(miniMessage.deserialize(" "));
                player.sendMessage(miniMessage.deserialize("<#e61717>⚔ <dark_gray>» <gray>The runners team has won the Game <#e61717>" + killer.getName() + " <gray>has defeated the Ender Dragon!"));
                player.sendMessage(miniMessage.deserialize("<#e61717>⚔ <dark_gray>»<gray> World seed <dark_gray>»<#e61717>" + seed));
                player.sendMessage(miniMessage.deserialize("<#e61717>⚔ <dark_gray>»<gray> Game Duration <dark_gray>»<#e61717>" + elapsedTime));
                player.sendMessage(miniMessage.deserialize(" "));
            }
        }
    }
}
