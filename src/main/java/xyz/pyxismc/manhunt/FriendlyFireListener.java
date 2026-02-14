package xyz.pyxismc.manhunt;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class FriendlyFireListener implements Listener {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final ManhuntGUI manhuntGUI;
    private boolean friendlyFireEnabled = false; // Default: disabled

    public FriendlyFireListener(ManhuntGUI manhuntGUI) {
        this.manhuntGUI = manhuntGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Reliable title check using PlainText
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        if (title.contains("Manhunt configuration")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.FIRE_CHARGE) {
                // Check if the item has the correct display name
                if (clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
                    String displayName = PlainTextComponentSerializer.plainText().serialize(clicked.getItemMeta().displayName());

                    if (displayName.equals("Hunters Friendly Fire")) {
                        // Toggle state
                        friendlyFireEnabled = !friendlyFireEnabled;

                        // Send feedback message
                        if (friendlyFireEnabled) {
                            player.sendMessage(miniMessage.deserialize(":manhunt: <gradient:#e64935:#e69935>Friendly Fire: <#0fd612>ON"));
                        } else {
                            player.sendMessage(miniMessage.deserialize(":manhunt: <gradient:#e64935:#e69935>Friendly Fire:<#d60f0f> OFF"));
                        }

                        player.playSound(player.getLocation(), "minecraft:block.note_block.pling", 1.0f, 1.0f);

                        // Refresh the GUI immediately to show the new ON/OFF status
                        manhuntGUI.refreshConfigMenu(event.getInventory());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Only care about Player vs Player
        if (!(event.getEntity() instanceof Player victim) || !(event.getDamager() instanceof Player attacker)) return;

        // If FF is enabled, we don't need to block anything
        if (friendlyFireEnabled) return;

        // Check if both are hunters
        if (victim.hasPermission("manhunt.hunter") && attacker.hasPermission("manhunt.hunter")) {

            // Validate world
            String worldName = victim.getWorld().getName();
            if (worldName.equals("world") || worldName.equals("world_nether") || worldName.equals("world_the_end")) {

                event.setCancelled(true);
                attacker.sendMessage(miniMessage.deserialize("<red>Friendly fire is disabled!"));
            }
        }
    }

    public boolean isFriendlyFireEnabled() {
        return friendlyFireEnabled;
    }

    public void setFriendlyFireEnabled(boolean enabled) {
        this.friendlyFireEnabled = enabled;
    }
}