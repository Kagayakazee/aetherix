package xd.kagayakazee.aetherix.govno;

import com.github.retrooper.packetevents.PacketEvents;
import xd.kagayakazee.aetherix.Main;
import xd.kagayakazee.aetherix.commands.CommandManager;
import xd.kagayakazee.aetherix.config.ConfigManager;
import xd.kagayakazee.aetherix.listeners.PacketListener;
import xd.kagayakazee.aetherix.player.PlayerDataManager;
import xd.kagayakazee.aetherix.punishments.PunishmentManager;


public class PluginInitializer {
    
    private final Main plugin;
    
    public PluginInitializer(Main plugin) {
        this.plugin = plugin;
    }

    public void initializePlugin() {
        ConfigManager configManager = new ConfigManager(plugin);
        plugin.setConfigManager(configManager);
        PunishmentManager punishmentManager = new PunishmentManager(configManager);
        plugin.setPunishmentManager(punishmentManager);
        PlayerDataManager playerDataManager = new PlayerDataManager();
        plugin.setPlayerDataManager(playerDataManager);
        CommandManager commandManager = new CommandManager();
        plugin.setCommandManager(commandManager);
        commandManager.registerCommands(plugin);
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener(playerDataManager));
        PacketEvents.getAPI().init();


        plugin.getServer().getPluginManager().registerEvents(playerDataManager, plugin);

    }
}
