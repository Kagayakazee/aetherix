package xd.kagayakazee.aetherix.checks;


import org.bukkit.Bukkit;
import xd.kagayakazee.aetherix.Main;
import xd.kagayakazee.aetherix.player.PlayerData;
import xd.kagayakazee.aetherix.punishments.PunishmentCommand;
import xd.kagayakazee.aetherix.punishments.PunishmentProfile;
import xd.kagayakazee.aetherix.utils.AlertManager;
//import xd.kagayakazee.gulac.utils.SetbackManager;

public abstract class Check {

    protected final PlayerData playerData;
    private final String name;

    private int violations;
    private long lastViolationTime;

    public Check(PlayerData playerData, String name) {
        this.playerData = playerData;
        this.name = name;
    }

    protected void flag(String debugInfo) {
        this.violations++;
        this.lastViolationTime = System.currentTimeMillis();

        PunishmentProfile profile = Main.getInstance().getPunishmentManager().getProfileForCheck(this.name);
        if (profile == null) return;

        for (PunishmentCommand pCmd : profile.getCommands()) {
            if (pCmd.shouldExecute(this.violations)) {
                executeCommand(pCmd, debugInfo);
            }
        }
    }
//    protected void setback() {
//
//        this.playerData.setLastSetbackTime();
//
//        SetbackManager.performSetback(this.playerData);
//    }
    protected void decay() {
        PunishmentProfile profile = Main.getInstance().getPunishmentManager().getProfileForCheck(this.name);
        if (profile == null || violations == 0) return;
        long timeSinceLastFlag = (System.currentTimeMillis() - lastViolationTime) / 1000;
        if (timeSinceLastFlag > profile.removeViolationsAfter) {
            this.violations = 0;
        }
    }

    private void executeCommand(PunishmentCommand pCmd, String debugInfo) {
        String command = pCmd.command
                .replace("%player%", playerData.player.getName())
                .replace("%check_name%", this.name)
                .replace("%vl%", String.valueOf(this.violations));

        if (command.equalsIgnoreCase("[alert]")) {
            AlertManager.sendAlert(playerData.player, this.name, debugInfo, this.violations);
            return;
        }

        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        });
    }

    public String getName() {
        return name;
    }
}