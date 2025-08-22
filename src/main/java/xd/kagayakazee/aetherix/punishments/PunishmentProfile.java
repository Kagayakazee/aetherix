package xd.kagayakazee.aetherix.punishments;

import java.util.ArrayList;
import java.util.List;


public class PunishmentProfile {
    public final int removeViolationsAfter;
    private final List<String> checks;
    private final List<PunishmentCommand> commands = new ArrayList<>();

    public PunishmentProfile(int removeAfter, List<String> checks, List<String> commandStrings) {
        this.removeViolationsAfter = removeAfter;
        this.checks = checks;

        for (String cmdStr : commandStrings) {
            String[] parts1 = cmdStr.split(":", 2);
            if (parts1.length != 2) continue;

            String[] parts2 = parts1[1].split(" ", 2);
            if (parts2.length != 2) continue;

            try {
                int threshold = Integer.parseInt(parts1[0]);
                int interval = Integer.parseInt(parts2[0]);
                String command = parts2[1];
                commands.add(new PunishmentCommand(threshold, interval, command));
            } catch (NumberFormatException ignored) {}
        }
    }

    public boolean appliesTo(String checkName) {
        return checks.stream().anyMatch(checkName::equalsIgnoreCase);
    }

    public List<PunishmentCommand> getCommands() {
        return commands;
    }
}