package xd.kagayakazee.aetherix.listeners;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.entity.Player;
import xd.kagayakazee.aetherix.checks.type.MovementCheck;
import xd.kagayakazee.aetherix.checks.type.PacketCheck;
import xd.kagayakazee.aetherix.checks.type.RotationCheck;
import xd.kagayakazee.aetherix.player.PlayerData;
import xd.kagayakazee.aetherix.player.PlayerDataManager;

public class PacketListener extends PacketListenerAbstract {
    private final PlayerDataManager dataManager;

    public PacketListener(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        final Player player = (Player) event.getPlayer();

        if (player.hasPermission("aetherix.exempt")) {
            return;
        }

        final PlayerData data = dataManager.get(player);
        if (data == null) return;

        final PacketType.Play.Client packetType = (PacketType.Play.Client) event.getPacketType();
        boolean attackPacketReceived = false;

        if (packetType.equals(PacketType.Play.Client.USE_ITEM)) {
            if (data.isUsingItem()) {
                data.setUsingItemBuffer(5);
            }
        } else if (packetType.equals(PacketType.Play.Client.PLAYER_DIGGING)) {
            WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);
            if (wrapper.getAction() == DiggingAction.RELEASE_USE_ITEM) {
                data.setUsingItemBuffer(0);
            }
        } else if (packetType.equals(PacketType.Play.Client.HELD_ITEM_CHANGE)) {
            data.setUsingItemBuffer(0);
            WrapperPlayClientHeldItemChange wrapper = new WrapperPlayClientHeldItemChange(event);
            data.setHeldItem(SpigotConversionUtil.fromBukkitItemStack(player.getInventory().getItem(wrapper.getSlot())));
        } else if (packetType.equals(PacketType.Play.Client.ENTITY_ACTION)) {
            WrapperPlayClientEntityAction wrapper = new WrapperPlayClientEntityAction(event);
            switch (wrapper.getAction()) {
                case START_SPRINTING: data.updateStates(true, data.isSneaking()); break;
                case STOP_SPRINTING: data.updateStates(false, data.isSneaking()); break;
                case START_SNEAKING: data.updateStates(data.isSprinting(), true); break;
                case STOP_SNEAKING: data.updateStates(data.isSprinting(), false); break;
            }
        } else if (packetType.equals(PacketType.Play.Client.INTERACT_ENTITY)) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
            if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                attackPacketReceived = true;
            }
        }

        for (PacketCheck check : data.getCheckManager().getPacketChecks()) {
            check.onPacketReceive(event);
        }

        if (WrapperPlayClientPlayerFlying.isFlying(packetType)) {
            data.incrementTicksSinceServerPosAdjust();
            WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
            float lastYaw = data.getYaw();
            float lastPitch = data.getPitch();

            boolean positionChanged = wrapper.hasPositionChanged();
            boolean rotationChanged = wrapper.hasRotationChanged();


            if (positionChanged) {
                data.updatePosition(wrapper.getLocation().getX(), wrapper.getLocation().getY(), wrapper.getLocation().getZ(), wrapper.isOnGround());
            }
            if (rotationChanged) {
                data.updateRotation(wrapper.getLocation().getYaw(), wrapper.getLocation().getPitch());
            }

            if (rotationChanged) {
                data.getRotationProcessor().handle(data.getYaw(), data.getPitch());
                data.getAimProcessor().handle(Math.abs(data.getPitch() - lastPitch), Math.abs(data.getYaw() - lastYaw));

                for (RotationCheck check : data.getCheckManager().getRotationChecks()) {
                    check.onRotation(data, null);
                }
            }

            if (positionChanged) {

                data.updateHorizontalSpeed();
                data.updateEnvironmentState();
                data.updateClimbableState();

                for (MovementCheck check : data.getCheckManager().getMovementChecks()) {
                    check.onMove(data);
                }

                data.updateLastClientVelocity();
            }

            if (data.getUsingItemBuffer() > 0) {
                data.setUsingItemBuffer(data.getUsingItemBuffer() - 1);
            }
            data.getPacketStateData().setHittiks(data.getPacketStateData().getHittiks() + 1);
        }

        if (attackPacketReceived) {
            data.getPacketStateData().setHittiks(0);
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        final Player player = (Player) event.getPlayer();

        if (player.hasPermission("aetherix.exempt")) {
            return;
        }

        final PlayerData data = dataManager.get(player);
        if (data == null) return;

        final PacketType.Play.Server packetType = (PacketType.Play.Server) event.getPacketType();

        if (packetType.equals(PacketType.Play.Server.ENTITY_VELOCITY)) {
            WrapperPlayServerEntityVelocity wrapper = new WrapperPlayServerEntityVelocity(event);
            if (wrapper.getEntityId() == player.getEntityId()) {
                data.setLastHitTime();
            }
        }

        if (packetType.equals(PacketType.Play.Server.PLAYER_POSITION_AND_LOOK)) {
            data.setLastTeleportTime();
            data.resetTicksSinceServerPosAdjust();
        }

        for (PacketCheck check : data.getCheckManager().getPacketChecks()) {
            check.onPacketSend(event);
        }
    }
}