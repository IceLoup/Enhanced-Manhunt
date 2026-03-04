package xyz.pyxismc.manhunt.GUIs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.PermissionAttachment;
import xyz.pyxismc.manhunt.Manhunt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SelectionGUI implements Listener {

    private final Manhunt plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final String MENU_TITLE = "★ Manhunt Menu";

    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public SelectionGUI(Manhunt plugin) {
        this.plugin = plugin;
    }

    private String safeName(Player p) {
        return MiniMessage.miniMessage().escapeTags(p.getName());
    }

    private PermissionAttachment getAttachment(Player target) {
        return attachments.computeIfAbsent(
                target.getUniqueId(),
                k -> target.addAttachment(plugin)
        );
    }

    private void clearRoles(Player target) {
        PermissionAttachment att = getAttachment(target);
        att.unsetPermission("manhunt.runner");
        att.unsetPermission("manhunt.hunter");
    }

    public void openMenu(Player viewer) {
        Inventory menu = Bukkit.createInventory(null, 54,
                miniMessage.deserialize("<gradient:#0fb800:#39db6c> Runner selection"));

        int slot = 0;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (slot >= 45) break; // Reserve bottom row for UI

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(target);

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());

            if (target.hasPermission("manhunt.runner")) {

                meta.displayName(miniMessage.deserialize(
                        "<gradient:#3498EB:#34BDEB><!ITALIC><bold>ʀᴜɴɴᴇʀ <reset><!ITALIC><gray>" + safeName(target)));

                lore.add(miniMessage.deserialize("<gray>Current Role <dark_gray>» <gradient:#3498eb:#34bdeb><bold>ʀᴜɴɴᴇʀ"));
                lore.add(Component.empty());
                lore.add(miniMessage.deserialize("<dark_gray>» <white>Click <gray>change to <gray><bold>ꜱᴘᴇᴄᴛᴀᴛᴏʀ"));

            } else if (target.hasPermission("manhunt.hunter")) {

                meta.displayName(miniMessage.deserialize(
                        "<gradient:#660000:#FF2D2D><!ITALIC><bold>ʜᴜɴᴛᴇʀ <reset><!ITALIC><gray>" + safeName(target)));

                lore.add(miniMessage.deserialize("<gray>Current Role <dark_gray>» <gradient:#660000:#FF2D2D><bold>ʜᴜɴᴛᴇʀ"));
                lore.add(Component.empty());
                lore.add(miniMessage.deserialize("<dark_gray>» <white>Click <gray>change to <gradient:#3498eb:#34bdeb><bold>ʀᴜɴɴᴇʀ"));

            } else {

                meta.displayName(miniMessage.deserialize(
                        "<gray><!ITALIC><bold>ꜱᴘᴇᴄᴛᴀᴛᴏʀ <reset><gray><!ITALIC>" + safeName(target)));

                lore.add(miniMessage.deserialize("<gray>Current Role <dark_gray>» <gray><bold>ꜱᴘᴇᴄᴛᴀᴛᴏʀ"));
                lore.add(Component.empty());
                lore.add(miniMessage.deserialize("<dark_gray>» <white>Click <gray>change to <gradient:#660000:#FF2D2D><bold>ʜᴜɴᴛᴇʀ"));

            }

            meta.lore(lore.stream()
                    .map(c -> c.decoration(TextDecoration.ITALIC, false))
                    .toList());

            skull.setItemMeta(meta);
            menu.setItem(slot++, skull);
        }

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        filler.editMeta(m -> m.displayName(Component.empty()));
        for (int i = 45; i <= 53; i++) {
            if (i != 49) menu.setItem(i, filler);
        }

        ItemStack returnItem = new ItemStack(Material.RED_DYE);
        returnItem.editMeta(m -> m.displayName(
                miniMessage.deserialize("<!ITALIC><#FF2D2D>Close the menu")));
        menu.setItem(49, returnItem);

        viewer.openInventory(menu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = PlainTextComponentSerializer.plainText()
                .serialize(event.getView().title());

        if (title.contains(MENU_TITLE)) {
            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.DIAMOND_BOOTS) {
                Bukkit.getScheduler().runTask(plugin, () -> openMenu(player));
            }
            return;
        }

        if (!title.contains("Runner selection")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.RED_DYE) {
            player.playSound(player.getLocation(), "minecraft:ui.button.click", 1.0f, 1.0f);
            player.closeInventory();
            return;
        }

        if (clicked.getType() != Material.PLAYER_HEAD) return;

        player.playSound(player.getLocation(), "minecraft:block.note_block.pling", 1.0f, 1.0f);

        SkullMeta meta = (SkullMeta) clicked.getItemMeta();
        if (meta == null || meta.getOwningPlayer() == null) return;

        Player target = meta.getOwningPlayer().getPlayer();
        if (target == null) return;

        boolean isHunter = target.hasPermission("manhunt.hunter");
        boolean isRunner = target.hasPermission("manhunt.runner");

        clearRoles(target);
        PermissionAttachment att = getAttachment(target);

        if (!isHunter && !isRunner) {
            att.setPermission("manhunt.hunter", true);
        } else if (isHunter) {
            att.setPermission("manhunt.runner", true);
        }

        target.recalculatePermissions();
        Bukkit.getScheduler().runTask(plugin, () -> openMenu(player));
    }
}


