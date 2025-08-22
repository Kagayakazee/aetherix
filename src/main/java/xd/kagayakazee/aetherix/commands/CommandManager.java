package xd.kagayakazee.aetherix.commands;

import co.aikar.commands.PaperCommandManager;
import xd.kagayakazee.aetherix.Main;


public class CommandManager {

    public void registerCommands(Main plugin) {
        PaperCommandManager manager = new PaperCommandManager(plugin);
        manager.registerCommand(new AlertsCommand());
        manager.registerCommand(new Reload());
        manager.registerCommand(new Prob());
        manager.registerCommand(new DataCollectCommand());
    }
}
