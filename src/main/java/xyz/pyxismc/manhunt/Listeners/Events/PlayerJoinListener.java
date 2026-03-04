package xyz.pyxismc.manhunt.Listeners.Events;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private final Set<UUID> joinedThisSession = new HashSet<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!joinedThisSession.contains(player.getUniqueId())) {
            World lobbyWorld = Bukkit.getWorld("lobby");

            if (lobbyWorld != null) {
                player.teleport(lobbyWorld.getSpawnLocation());
            } else {
                Bukkit.getLogger().warning("Lobby world not found! Could not teleport " + player.getName());
            }

            joinedThisSession.add(player.getUniqueId());
        }
    }
}