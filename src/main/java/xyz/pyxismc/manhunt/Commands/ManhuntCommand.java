package xyz.pyxismc.manhunt.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.jetbrains.annotations.NotNull;
import xyz.pyxismc.manhunt.Manhunt;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ManhuntCommand implements CommandExecutor {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Manhunt plugin;
    private final Map<UUID, PermissionAttachment> permissions = new HashMap<>();

    public ManhuntCommand(Manhunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("manhunt.admin")) {
            Component noPermMessage = miniMessage.deserialize("<red>You don't have permission to use this command!");
            player.sendMessage(noPermMessage);
            return true;
        }

        if (args.length != 1) {
            Component usageMessage = miniMessage.deserialize("<red>Usage: /runner <player>");
            player.sendMessage(usageMessage);
            return true;
        }

        String targetName = args[0];
        Player targetPlayer = Bukkit.getPlayer(targetName);

        if (targetPlayer == null) {
            Component notFoundMessage = miniMessage.deserialize("<red>Player <yellow>" + targetName + "</yellow> not found or is offline!");
            player.sendMessage(notFoundMessage);
            return true;
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            removeRunnerPermission(onlinePlayer);
        }

        setRunnerPermission(targetPlayer);

        Component senderMessage = miniMessage.deserialize(
                "<green>:manhunt: You have set <yellow>" + targetPlayer.getName() + "</yellow> as the runner!"
        );
        player.sendMessage(senderMessage);

        Component targetMessage = miniMessage.deserialize(
                "<green>:manhunt: You have been selected as the <yellow>RUNNER</yellow>! Run and survive!"
        );
        targetPlayer.sendMessage(targetMessage);

        Component hunterMessage = miniMessage.deserialize(
                "<red>:manhunt: <yellow>" + targetPlayer.getName() + "</yellow> is now the runner! Hunt them down!"
        );
        for (Player hunter : Bukkit.getOnlinePlayers()) {
            if (hunter.hasPermission("manhunt.hunter") && !hunter.equals(targetPlayer)) {
                hunter.sendMessage(hunterMessage);
            }
        }

        return true;
    }

    public void setRunnerPermission(Player player) {
        UUID playerId = player.getUniqueId();

        if (permissions.containsKey(playerId)) {
            permissions.get(playerId).remove();
        }

        PermissionAttachment attachment = player.addAttachment(plugin);
        attachment.setPermission("manhunt.runner", true);
        attachment.setPermission("manhunt.hunter", false);
        permissions.put(playerId, attachment);
    }

    public void removeRunnerPermission(Player player) {
        UUID playerId = player.getUniqueId();

        if (permissions.containsKey(playerId)) {
            PermissionAttachment attachment = permissions.get(playerId);
            attachment.setPermission("manhunt.runner", false);

            attachment.setPermission("manhunt.hunter", true);
        }
    }

    public void cleanup() {
        for (PermissionAttachment attachment : permissions.values()) {
            attachment.remove();
        }
        permissions.clear();
    }

}
