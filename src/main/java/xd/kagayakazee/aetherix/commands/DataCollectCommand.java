package xd.kagayakazee.aetherix.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xd.kagayakazee.aetherix.checks.combat.DataCollectorCheck;


@CommandAlias("aetherix")
@CommandPermission("aetherix.datacollect")
public class DataCollectCommand extends BaseCommand {

    @Subcommand("datacollect start")
    @CommandCompletion("@players LEGIT|CHEAT")
    @Syntax("<player> <LEGIT|CHEAT>")
    @Description("Начать сбор данных для игрока.")
    public void onStart(CommandSender sender, OnlinePlayer target, String status) {
        Player targetPlayer = target.getPlayer();
        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.RED + "Игрок не найден.");
            return;
        }

        if (!status.equalsIgnoreCase("LEGIT") && !status.equalsIgnoreCase("CHEAT")) {
            sender.sendMessage(ChatColor.RED + "Неверный статус. Используйте LEGIT или CHEAT.");
            return;
        }

        boolean success = DataCollectorCheck.startCollecting(targetPlayer.getUniqueId(), targetPlayer.getName(), status.toUpperCase());
        if (success) {
            sender.sendMessage(ChatColor.GREEN + "Сбор данных для " + targetPlayer.getName() + " начат со статусом: " + status.toUpperCase());
        } else {
            sender.sendMessage(ChatColor.RED + "Сбор данных для " + targetPlayer.getName() + " уже идет.");
        }
    }

    @Subcommand("datacollect stop")
    @CommandCompletion("@players")
    @Syntax("<player>")
    @Description("Остановить сбор данных и сохранить файл.")
    public void onStop(CommandSender sender, OnlinePlayer target) {
        Player targetPlayer = target.getPlayer();
        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.RED + "Игрок не найден.");
            return;
        }

        boolean success = DataCollectorCheck.stopCollecting(targetPlayer.getUniqueId());
        if (success) {
            sender.sendMessage(ChatColor.GREEN + "Сбор данных для " + targetPlayer.getName() + " остановлен. Файл сохранен.");
        } else {
            sender.sendMessage(ChatColor.RED + "Сбор данных для " + targetPlayer.getName() + " не был запущен.");
        }
    }
}
