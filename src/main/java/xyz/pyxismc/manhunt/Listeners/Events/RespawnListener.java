package xyz.pyxismc.manhunt.Listeners.Events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class RespawnListener implements Listener {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final JavaPlugin plugin;

    public RespawnListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("manhunt.hunter")) {
            ItemStack compass = new ItemStack(Material.COMPASS);
            compass.editMeta(meta -> {
                meta.displayName(miniMessage.deserialize("<!italic><#d60f0f>Compass Tracker"));

                List<Component> lore = new ArrayList<>();
                lore.add(miniMessage.deserialize("<!italic><dark_gray>» <gray>Left-Click to <#d60f0f>update location"));
                lore.add(miniMessage.deserialize("<!italic><dark_gray>» <gray>Right-Click to <#d60f0f>switch runner"));
                meta.lore(lore);
            });
            player.getInventory().addItem(compass);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin,
                    () -> player.setGameMode(GameMode.SPECTATOR), 1L);
        }
    }
}
