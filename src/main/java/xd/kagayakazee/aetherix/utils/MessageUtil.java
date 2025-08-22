package xd.kagayakazee.aetherix.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

import xd.kagayakazee.aetherix.Main;



public final class MessageUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static Main plugin;

    private MessageUtil() {}

    public static void init(Main mainPlugin) {
        plugin = mainPlugin;
    }
    public static Component miniMessage(String message) {
        return MINI_MESSAGE.deserialize(message);
    }

    public static void sendMessage(CommandSender sender, String message) {
        plugin.adventure().sender(sender).sendMessage(miniMessage(message));
    }

    public static void sendActionBar(CommandSender sender, String message) {
        plugin.adventure().sender(sender).sendActionBar(miniMessage(message));
    }
}
