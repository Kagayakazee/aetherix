package xd.kagayakazee.aetherix.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xd.kagayakazee.aetherix.Main;

import java.io.File;



public class ConfigManager {

    private final Main plugin;
    private FileConfiguration cfg;
    private FileConfiguration messagesConfig;
    private FileConfiguration punishmentsConfig;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    public void loadConfigs() {
        File cfgfile = new File(plugin.getDataFolder(), "config.yml");
        if (!cfgfile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        cfg = YamlConfiguration.loadConfiguration(cfgfile);



        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        File punishmentsFile = new File(plugin.getDataFolder(), "punishments.yml");
        if (!punishmentsFile.exists()) {
            plugin.saveResource("punishments.yml", false);
        }
        punishmentsConfig = YamlConfiguration.loadConfiguration(punishmentsFile);
    }

    public void reload() {
        loadConfigs();
    }



    public String getMessage(String path) {
        return messagesConfig.getString(path, "&cMessage not found: " + path);
    }
    public FileConfiguration getConfig() {
        return cfg;
    }
    public FileConfiguration getPunishmentsConfig() {
        return punishmentsConfig;
    }
}