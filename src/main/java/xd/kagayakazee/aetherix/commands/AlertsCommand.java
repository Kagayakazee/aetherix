package xd.kagayakazee.aetherix.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xd.kagayakazee.aetherix.player.PlayerData;



@CommandAlias("aetherix")
public class AlertsCommand extends BaseCommand {

    @Subcommand("alerts")
    @Description("Включить/выключить алерты для себя")
    @CommandPermission("aetherix.alerts.toggle")
    public void alerts(Player player) {
        PlayerData playerData = plugin.getPlayerDataManager().get(player);
        
        if (playerData == null) {
            player.sendMessage(ChatColor.RED + "Ошибка получения данных игрока!");
            return;
        }
        boolean currentStatus = playerData.isReceiveAlerts();
        boolean newStatus = !currentStatus;
        playerData.setReceiveAlerts(newStatus);
        String statusText = newStatus ? ChatColor.GREEN + "включены" : ChatColor.RED + "выключены";
        String prefix = plugin.getConfigManager().getMessage("prefix");
        
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            prefix + " Алерты для вас " + statusText + ChatColor.GRAY + "!"));
    }

    @Subcommand("alerts status")
    @Description("Посмотреть текущий статус алертов")
    @CommandPermission("aetherix.alerts.toggle")
    public void alertsStatus(Player player) {
        PlayerData playerData = plugin.getPlayerDataManager().get(player);
        
        if (playerData == null) {
            player.sendMessage(ChatColor.RED + "Ошибка получения данных игрока!");
            return;
        }

        boolean currentStatus = playerData.isReceiveAlerts();
        String statusText = currentStatus ? ChatColor.GREEN + "включены" : ChatColor.RED + "выключены";
        String prefix = plugin.getConfigManager().getMessage("prefix");
        
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            prefix + " Алерты для вас " + statusText + ChatColor.GRAY + "."));
    }
}
