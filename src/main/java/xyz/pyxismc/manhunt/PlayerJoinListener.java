package xyz.pyxismc.manhunt;

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

    // Tracks players who have joined during this server session
    private final Set<UUID> joinedThisSession = new HashSet<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check if the player has NOT joined since the last server restart
        if (!joinedThisSession.contains(player.getUniqueId())) {
            World lobbyWorld = Bukkit.getWorld("lobby");

            if (lobbyWorld != null) {
                player.teleport(lobbyWorld.getSpawnLocation());
            } else {
                // Optional: Log a warning if the lobby world isn't found
                Bukkit.getLogger().warning("Lobby world not found! Could not teleport " + player.getName());
            }

            // Mark this player as having joined this session
            joinedThisSession.add(player.getUniqueId());
        }
    }
}