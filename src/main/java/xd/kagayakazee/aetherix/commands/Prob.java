package xd.kagayakazee.aetherix.commands;


import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import xd.kagayakazee.aetherix.Main;
import xd.kagayakazee.aetherix.checks.combat.AICheck;
import xd.kagayakazee.aetherix.player.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;



@CommandAlias("aetherix")
@CommandPermission("aetherix.prob")
public class Prob extends BaseCommand {

    private static final Map<UUID, UUID> observing = new HashMap<>();
    private static final Map<UUID, BukkitTask> observerTasks = new HashMap<>();

    @Subcommand("prob")
    @CommandCompletion("@players")
    @Syntax("<player>")
    @Description("Начать/остановить отображение вероятности и VL для игрока.")
    public void onProb(CommandSender sender, OnlinePlayer target) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эту команду может использовать только игрок.");
            return;
        }

        Player observer = (Player) sender;
        Player targetPlayer = target.getPlayer();

        if (observing.containsKey(observer.getUniqueId()) && observing.get(observer.getUniqueId()).equals(targetPlayer.getUniqueId())) {
            stopObserving(observer);
            observer.sendMessage(ChatColor.YELLOW + "Отображение данных для " + targetPlayer.getName() + " отключено.");
            return;
        }

        if (observerTasks.containsKey(observer.getUniqueId())) {
            stopObserving(observer);
        }

        startObserving(observer, targetPlayer);
        observer.sendMessage(ChatColor.GREEN + "Отображение данных для " + targetPlayer.getName() + " включено.");
    }

    private void startObserving(Player observer, Player target) {
        observing.put(observer.getUniqueId(), target.getUniqueId());

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                Player currentObserver = Bukkit.getPlayer(observer.getUniqueId());
                Player currentTarget = Bukkit.getPlayer(target.getUniqueId());

                if (currentObserver == null || !currentObserver.isOnline() || currentTarget == null || !currentTarget.isOnline()) {
                    stopObserving(observer);
                    return;
                }

                PlayerData targetData = Main.getInstance().getPlayerDataManager().get(currentTarget);
                if (targetData == null) {
                    sendActionBarMessage(currentObserver, "&cНе удалось получить данные для " + currentTarget.getName());
                    return;
                }

                AICheck aiCheck = targetData.getCheckManager().getCheck(AICheck.class);
                if (aiCheck == null) {
                    sendActionBarMessage(currentObserver, "&cAI-Чек не активен для " + currentTarget.getName());
                    return;
                }

                double probability = aiCheck.getLastProbability();
                double violationLevel = aiCheck.getViolationLevel();
                long latency = aiCheck.getLastApiLatency();

                String probColor;
                if (probability > 0.9) probColor = "&c";
                else if (probability > 0.5) probColor = "&e";
                else probColor = "&a";

                String vlColor;
                if (violationLevel > 30) vlColor = "&4&l";
                else if (violationLevel > 15) vlColor = "&c";
                else if (violationLevel > 5) vlColor = "&e";
                else vlColor = "&a";


                String latencyColor;
                String latencyText;
                if (latency < 0) {
                    latencyColor = "&7";
                    latencyText = "N/A";
                } else {
                    if (latency > 500) latencyColor = "&c";
                    else if (latency > 200) latencyColor = "&e";
                    else latencyColor = "&a";
                    latencyText = latency + "ms";
                }

                String message = String.format("%sProb (%s): %.4f %s| %sVL: %.2f %s| %sLatency: %s",
                        probColor,
                        currentTarget.getName(),
                        probability,
                        "&7",
                        vlColor,
                        violationLevel,
                        "&7",
                        latencyColor,
                        latencyText);

                sendActionBarMessage(currentObserver, message);
            }
        }.runTaskTimer(Main.getInstance(), 0L, 2L);

        observerTasks.put(observer.getUniqueId(), task);
    }

    private void stopObserving(Player observer) {
        observing.remove(observer.getUniqueId());
        BukkitTask existingTask = observerTasks.remove(observer.getUniqueId());
        if (existingTask != null) {
            existingTask.cancel();
        }
        sendActionBarMessage(observer, " ");
    }

    private void sendActionBarMessage(Player player, String message) {
        String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(coloredMessage));
    }
}