package xd.kagayakazee.aetherix.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class PlayerDataManager implements Listener {

    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();


    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerDataMap.put(player.getUniqueId(), new PlayerData(player));

    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerDataMap.remove(player.getUniqueId());
//        Main.getInstance().getLogger().info("Removed PlayerData for " + player.getName());
    }



    public PlayerData get(Player player) {
        if (player == null) {
            return null;
        }
        return playerDataMap.get(player.getUniqueId());
    }
}