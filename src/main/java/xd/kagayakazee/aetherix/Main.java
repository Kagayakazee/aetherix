package xd.kagayakazee.aetherix;

import com.github.retrooper.packetevents.PacketEvents;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import xd.kagayakazee.aetherix.utils.MessageUtil;

import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import xd.kagayakazee.aetherix.commands.CommandManager;
import xd.kagayakazee.aetherix.config.ConfigManager;
import xd.kagayakazee.aetherix.player.PlayerDataManager;
import xd.kagayakazee.aetherix.govno.PluginInitializer;
import xd.kagayakazee.aetherix.punishments.PunishmentManager;


public class Main extends JavaPlugin {
    private static Main instance;



    private BukkitAudiences adventure;
    private PlayerDataManager playerDataManager;
    private ConfigManager configManager;
    private PunishmentManager punishmentManager;
    private CommandManager commandManager;

    @Override
    public void onLoad() {
        instance = this;
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        this.adventure = BukkitAudiences.create(this);
        MessageUtil.init(this);
        PluginInitializer initializer = new PluginInitializer(this);
        initializer.initializePlugin();
//        SetbackManager.initialize();
    }

    @Override
    public void onDisable() {
        if (PacketEvents.getAPI() != null && PacketEvents.getAPI().isLoaded()) {
            PacketEvents.getAPI().terminate();
        }
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    public BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    public static Main getInstance() {
        return instance;
    }


    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public void setPunishmentManager(PunishmentManager punishmentManager) {
        this.punishmentManager = punishmentManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public void setPlayerDataManager(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }
}