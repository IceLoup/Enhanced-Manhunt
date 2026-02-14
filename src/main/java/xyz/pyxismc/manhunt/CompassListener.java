package xyz.pyxismc.manhunt;

import net.kyori.adventure.text.Component;
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
            hunter.sendMessage(miniMessage.deserialize("<red>Compass tracking is disabled!"));
            return;
        }

        List<Player> runners = getAllRunners();
        if (runners.isEmpty()) {
            hunter.sendMessage(miniMessage.deserialize("<red>No runners found online!"));
            return;
        }

        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            // Logic: Switch to the next runner
            switchTarget(hunter, runners);
        } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            // Logic: Just update the position of the current target
            updateCompass(hunter);
        }
    }

    private void switchTarget(Player hunter, List<Player> runners) {
        UUID currentTargetId = hunterTargets.get(hunter.getUniqueId());
        Player nextRunner;

        if (currentTargetId == null) {
            nextRunner = runners.get(0);
        } else {
            // Find the index of the current target and get the next one in the list
            int currentIndex = -1;
            for (int i = 0; i < runners.size(); i++) {
                if (runners.get(i).getUniqueId().equals(currentTargetId)) {
                    currentIndex = i;
                    break;
                }
            }
            // Cycle back to 0 if at the end of the list
            int nextIndex = (currentIndex + 1) % runners.size();
            nextRunner = runners.get(nextIndex);
        }

        hunterTargets.put(hunter.getUniqueId(), nextRunner.getUniqueId());
        hunter.sendMessage(miniMessage.deserialize("<green>Now tracking: <yellow>" + nextRunner.getName()));
        updateCompass(hunter);
    }

    private void updateCompass(Player hunter) {
        UUID targetId = hunterTargets.get(hunter.getUniqueId());

        // If no target selected yet, try to pick the first available runner
        if (targetId == null) {
            List<Player> runners = getAllRunners();
            if (runners.isEmpty()) return;
            targetId = runners.get(0).getUniqueId();
            hunterTargets.put(hunter.getUniqueId(), targetId);
        }

        Player runner = Bukkit.getPlayer(targetId);
        if (runner == null || !runner.isOnline()) {
            hunter.sendMessage(miniMessage.deserialize("<red>Target is offline! Right-click to switch."));
            return;
        }

        Location runnerLoc = runner.getLocation();
        World hunterWorld = hunter.getWorld();

        if (hunterWorld.equals(runnerLoc.getWorld())) {
            hunter.setCompassTarget(runnerLoc);
            hunter.sendMessage(miniMessage.deserialize("<gray>Compass updated for <white>" + runner.getName()));
        } else {
            // Cross-dimensional tracking
            Location lastLoc = lastKnownLocations.getOrDefault(targetId, new HashMap<>()).get(hunterWorld.getName());

            if (lastLoc != null) {
                hunter.setCompassTarget(lastLoc);
                hunter.sendMessage(miniMessage.deserialize("<yellow>" + runner.getName() + " is in " + runnerLoc.getWorld().getName() + "! Tracking last portal..."));
            } else {
                hunter.setCompassTarget(hunterWorld.getSpawnLocation());
                hunter.sendMessage(miniMessage.deserialize("<red>Target in another world. No last known location found."));
            }
        }
    }

    private List<Player> getAllRunners() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("manhunt.runner"))
                .collect(Collectors.toList());
    }
}