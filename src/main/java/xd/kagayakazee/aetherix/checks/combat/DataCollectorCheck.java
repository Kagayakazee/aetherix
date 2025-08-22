package xd.kagayakazee.aetherix.checks.combat;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import xd.kagayakazee.aetherix.checks.Check;
import xd.kagayakazee.aetherix.checks.type.PacketCheck;
import xd.kagayakazee.aetherix.player.PlayerData;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class DataCollectorCheck extends Check implements PacketCheck {

    private static final Map<UUID, DataSession> activeSessions = new ConcurrentHashMap<>();


    private boolean attackSentThisTick = false;

    public DataCollectorCheck(PlayerData playerData) {
        super(playerData, "DataCollector");
    }

    public static boolean startCollecting(UUID uuid, String playerName, String status) {
        if (activeSessions.containsKey(uuid)) {
            return false;
        }
        activeSessions.put(uuid, new DataSession(uuid, playerName, status));
        return true;
    }

    public static boolean stopCollecting(UUID uuid) {
        DataSession session = activeSessions.remove(uuid);
        if (session != null) {
            try {
                session.saveAndClose();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (playerData == null) return;
        DataSession session = activeSessions.get(playerData.getUuid());
        if (session == null) {
            return;
        }


        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
            if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                this.attackSentThisTick = true;
                // playerData.setAttackedThisTick(true);
            }
        }

        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (playerData.getPacketStateData().getHittiks() < 40) {
                session.addTick(new TickData(playerData, this.attackSentThisTick));
            }
            this.attackSentThisTick = false;
            // playerData.setAttackedThisTick(false);
        }
    }
}