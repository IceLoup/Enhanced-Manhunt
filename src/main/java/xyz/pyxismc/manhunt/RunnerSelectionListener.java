package xyz.pyxismc.manhunt;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RunnerSelectionListener implements Listener {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Plugin plugin;
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public RunnerSelectionListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Use PlainText for more reliable title checking
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        if (title.contains("Select Runner")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() != Material.PLAYER_HEAD) return;

            SkullMeta meta = (SkullMeta) clicked.getItemMeta();
            if (meta != null && meta.getOwningPlayer() != null) {
                Player targetPlayer = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
                if (targetPlayer == null) return;

                // Get or create permission attachment
                PermissionAttachment attachment = attachments.computeIfAbsent(
                        targetPlayer.getUniqueId(),
                        uuid -> targetPlayer.addAttachment(plugin)
                );

                // Toggle permissions
                if (targetPlayer.hasPermission("manhunt.runner")) {
                    // Runner -> Hunter
                    attachment.setPermission("manhunt.runner", false);
                    attachment.setPermission("manhunt.hunter", true);
                    player.sendMessage(miniMessage.deserialize("<gradient:#e64935:#e69935>" + targetPlayer.getName() + " <gray>is now a hunter"));
                } else if (targetPlayer.hasPermission("manhunt.hunter")) {
                    // Hunter -> Runner
                    attachment.setPermission("manhunt.hunter", false);
                    attachment.setPermission("manhunt.runner", true);
                    player.sendMessage(miniMessage.deserialize("<gradient:#e64935:#e69935>" + targetPlayer.getName() + " <gray>is now a runner"));
                } else {
                    // No permission -> Runner
                    attachment.setPermission("manhunt.runner", true);
                    player.sendMessage(miniMessage.deserialize("<gradient:#e64935:#e69935>" + targetPlayer.getName() + " <gray>is now a runner"));
                }
            }
        }
    }

    // Call this when plugin disables or when cleaning up
    public void cleanup() {
        attachments.values().forEach(PermissionAttachment::remove);
        attachments.clear();
    }
}
