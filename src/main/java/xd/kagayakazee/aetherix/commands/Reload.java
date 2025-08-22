package xd.kagayakazee.aetherix.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.command.CommandSender;
import xd.kagayakazee.aetherix.utils.MessageUtil;


@CommandAlias("aetherix")
public class Reload extends BaseCommand {

    @Subcommand("reload")
    @CommandPermission("aetherix.reload")
    public void onReload(CommandSender sender) {
        plugin.getConfigManager().reload();
        plugin.getPunishmentManager().load(plugin.getConfigManager());
        MessageUtil.sendMessage(sender, "<green>aetherix reloaded.</green>");
    }
}