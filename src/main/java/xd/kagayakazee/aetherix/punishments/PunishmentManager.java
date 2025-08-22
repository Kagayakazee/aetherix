package xd.kagayakazee.aetherix.punishments;

import org.bukkit.configuration.ConfigurationSection;
import xd.kagayakazee.aetherix.config.ConfigManager;
import java.util.ArrayList;
import java.util.List;


public class PunishmentManager {
    private final List<PunishmentProfile> profiles = new ArrayList<>();

    public PunishmentManager(ConfigManager configManager) {
        load(configManager);
    }

    public void load(ConfigManager configManager) {
        profiles.clear();
        ConfigurationSection section = configManager.getPunishmentsConfig().getConfigurationSection("Punishments");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection profileSection = section.getConfigurationSection(key);
            if (profileSection == null) continue;

            int removeAfter = profileSection.getInt("remove-violations-after", 300);
            List<String> checks = profileSection.getStringList("checks");
            List<String> commands = profileSection.getStringList("commands");

            profiles.add(new PunishmentProfile(removeAfter, checks, commands));
        }
    }

    public PunishmentProfile getProfileForCheck(String checkName) {
        return profiles.stream()
                .filter(p -> p.appliesTo(checkName))
                .findFirst()
                .orElse(null);
    }
}