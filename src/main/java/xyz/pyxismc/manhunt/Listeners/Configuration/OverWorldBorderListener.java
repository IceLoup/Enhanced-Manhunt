package xyz.pyxismc.manhunt.Listeners.Configuration;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import xyz.pyxismc.manhunt.GUIs.ManhuntGUI;

public class OverWorldBorderListener implements Listener {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final ManhuntGUI manhuntGUI;
    private int worldBorderSize = 5000; // Default: 10000 blocks

    public OverWorldBorderListener(ManhuntGUI manhuntGUI) {
        this.manhuntGUI = manhuntGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        if (title.contains("Manhunt configuration")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.GRASS_BLOCK) {
                if (clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
                    String displayName = PlainTextComponentSerializer.plainText().serialize(clicked.getItemMeta().displayName());

                    if (displayName.equals("OverWorld Border")) {
                        ClickType clickType = event.getClick();

                        if (clickType.isLeftClick()) {
                            worldBorderSize += 200;
                            player.sendMessage(miniMessage.deserialize(":manhunt: <gradient:#e64935:#e69935>OverWorld Border: <#0fd612>+" + 200 + " <gray>(" + worldBorderSize + " blocks)"));
                        } else if (clickType.isRightClick()) {
                            if (worldBorderSize > 200) {
                                worldBorderSize -= 200;
                                player.sendMessage(miniMessage.deserialize(":manhunt: <gradient:#e64935:#e69935>OverWorld Border: <#d60f0f>-" + 200 + " <gray>(" + worldBorderSize + " blocks)"));
                            } else {
                                player.sendMessage(miniMessage.deserialize("<red>World border cannot be smaller than 200 blocks!"));
                            }
                        }

                        applyWorldBorder();

                        player.playSound(player.getLocation(), "minecraft:block.note_block.pling", 1.0f, 1.0f);

                        manhuntGUI.refreshConfigMenu(event.getInventory());
                    }
                }
            }
        }
    }

    private void applyWorldBorder() {
        World world = Bukkit.getWorld("world");
        if (world != null) {
            WorldBorder border = world.getWorldBorder();
            border.setSize(worldBorderSize);
            border.setCenter(0, 0);
        }
    }

    public int getWorldBorderSize() {
        return worldBorderSize;
    }

    public void setWorldBorderSize(int size) {
        this.worldBorderSize = size;
        applyWorldBorder();
    }
}
