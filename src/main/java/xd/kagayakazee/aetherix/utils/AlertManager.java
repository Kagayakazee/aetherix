package xd.kagayakazee.aetherix.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xd.kagayakazee.aetherix.Main;
import xd.kagayakazee.aetherix.player.PlayerData;


public class AlertManager {

    private static final String ALERT_PERMISSION = "aetherix.alerts";

    public static void sendAlert(Player violator, String checkName, String debugInfo, double vl) {
        String alertFormat = Main.getInstance().getConfigManager().getMessage("alerts-format");
        String message = alertFormat
                .replace("%prefix%", Main.getInstance().getConfigManager().getMessage("prefix"))
                .replace("%player%", violator.getName())
                .replace("%check_name%", checkName)
                .replace("%vl%", String.valueOf((int) vl))
                .replace("%debug%", debugInfo);

        String formattedMessage = ChatColor.translateAlternateColorCodes('&', message);


        Bukkit.getConsoleSender().sendMessage(formattedMessage);


        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission(ALERT_PERMISSION))
                .filter(p -> {
                    PlayerData playerData = Main.getInstance().getPlayerDataManager().get(p);
                    return playerData != null && playerData.isReceiveAlerts();
                })
                .forEach(staff -> staff.sendMessage(formattedMessage));
    }
}