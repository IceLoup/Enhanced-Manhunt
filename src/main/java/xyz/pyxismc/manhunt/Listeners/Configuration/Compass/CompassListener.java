package xyz.pyxismc.manhunt.Listeners.Configuration.Compass;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import xyz.pyxismc.manhunt.Manhunt;

import java.util.*;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

public class CompassListener implements Listener {

    private final MiniMessage miniMessage = miniMessage();
    private final Manhunt plugin;
    private final CompassEnableListener compassEnableListener;

    // Track last known locations: <PlayerUUID, <WorldName, Location>>
    private final Map<UUID, Map<String, Location>> lastKnownLocations = new HashMap<>();

    // Track which runner each hunter is currently following: <HunterUUID, RunnerUUID>
    private final Map<UUID, UUID> hunterTargets = new HashMap<>();

    public CompassListener(Manhunt plugin, CompassEnableListener compassEnableListener) {
        this.plugin = plugin;
        this.compassEnableListener = compassEnableListener;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!compassEnableListener.isCompassEnabled()) return;

        Player player = event.getPlayer();
        Location to = event.getTo();

        if (to == null || to.getBlock().equals(event.getFrom().getBlock())) return;

        UUID playerId = player.getUniqueId();
        String worldName = to.getWorld().getName();

        lastKnownLocations.computeIfAbsent(playerId, k -> new HashMap<>()).put(worldName, to.clone());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player hunter = event.getPlayer();
        ItemStack item = event.getItem();

        // Validation
        if (item == null || item.getType() != Material.COMPASS) return;
        if (!hunter.hasPermission("manhunt.hunter")) return;

        if (!compassEnableListener.isCompassEnabled()) {
            hunter.sendMessage(miniMessage.deserialize("<#e61717>Compass tracking is disabled!"));
            return;
        }

        List<Player> runners = getAllRunners();
        if (runners.isEmpty()) {
            hunter.sendMessage(miniMessage.deserialize("<#e61717>No runners found online!"));
            return;
        }

        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            switchTarget(hunter, runners);
        } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            updateCompass(hunter);
        }
    }

    private void switchTarget(Player hunter, List<Player> runners) {
        UUID currentTargetId = hunterTargets.get(hunter.getUniqueId());
        Player nextRunner;

        if (currentTargetId == null) {
            nextRunner = runners.get(0);
        } else {
            int currentIndex = -1;
            for (int i = 0; i < runners.size(); i++) {
                if (runners.get(i).getUniqueId().equals(currentTargetId)) {
                    currentIndex = i;
                    break;
                }
            }
            int nextIndex = (currentIndex + 1) % runners.size();
            nextRunner = runners.get(nextIndex);
        }

        hunterTargets.put(hunter.getUniqueId(), nextRunner.getUniqueId());
        hunter.sendMessage(miniMessage.deserialize("<#e61717>⚔ <dark_gray>» <gray>Now tracking<#e61717> " + nextRunner.getName()));
        updateCompass(hunter);
    }

    private void updateCompass(Player hunter) {
        UUID targetId = hunterTargets.get(hunter.getUniqueId());

        if (targetId == null) {
            List<Player> runners = getAllRunners();
            if (runners.isEmpty()) return;
            targetId = runners.get(0).getUniqueId();
            hunterTargets.put(hunter.getUniqueId(), targetId);
        }

        Player runner = Bukkit.getPlayer(targetId);
        if (runner == null || !runner.isOnline()) {
            hunter.sendMessage(miniMessage.deserialize("<#e61717>Target is offline! Right-click to switch."));
            return;
        }

        Location runnerLoc = runner.getLocation();
        World hunterWorld = hunter.getWorld();

        if (hunterWorld.equals(runnerLoc.getWorld())) {
            hunter.setCompassTarget(runnerLoc);
            hunter.sendMessage(miniMessage.deserialize("<#e61717>⚔ <dark_gray>» <gray>Updated position of <#e61717>" + runner.getName()));
        } else {
            Location lastLoc = lastKnownLocations.getOrDefault(targetId, new HashMap<>()).get(hunterWorld.getName());

            if (lastLoc != null) {
                hunter.setCompassTarget(lastLoc);
                hunter.sendMessage(miniMessage.deserialize("<#e61717>⚔ <dark_gray>» <#e61717>" + runner.getName() + " <gray>is in another world. Trakcing portal location."));
            } else {
                hunter.setCompassTarget(hunterWorld.getSpawnLocation());
                hunter.sendMessage(miniMessage.deserialize("<#e61717>Target in another world. No last known location found."));
            }
        }
    }

    private List<Player> getAllRunners() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("manhunt.runner"))
                .collect(Collectors.toList());
    }
}