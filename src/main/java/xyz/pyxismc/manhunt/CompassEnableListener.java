package xyz.pyxismc.manhunt;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class CompassEnableListener implements Listener {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final ManhuntGUI manhuntGUI;
    private boolean compassEnabled = true; // Default: ON

    public CompassEnableListener(ManhuntGUI manhuntGUI) {
        this.manhuntGUI = manhuntGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Use PlainText to avoid gradient comparison issues
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        if (title.contains("Manhunt configuration")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.COMPASS) {
                // Toggle state (Only once!)
                compassEnabled = !compassEnabled;

                // Send feedback
                if (compassEnabled) {
                    player.sendMessage(miniMessage.deserialize(":manhunt: <gradient:#e64935:#e69935>Compass tracking: <#0fd612>ON"));
                } else {
                    player.sendMessage(miniMessage.deserialize(":manhunt: <gradient:#e64935:#e69935>Compass tracking:<#d60f0f> OFF"));
                }

                player.playSound(player.getLocation(), "minecraft:block.note_block.pling", 1.0f, 1.0f);

                // Refresh the GUI to update the lore text
                manhuntGUI.refreshConfigMenu(event.getInventory());
            }
        }
    }

    public boolean isCompassEnabled() {
        return compassEnabled;
    }

    public void setCompassEnabled(boolean enabled) {
        this.compassEnabled = enabled;
    }
}