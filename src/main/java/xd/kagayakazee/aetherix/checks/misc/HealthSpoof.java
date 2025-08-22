package xd.kagayakazee.aetherix.checks.misc;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.*;

import xd.kagayakazee.aetherix.checks.Check;
import xd.kagayakazee.aetherix.checks.type.PacketCheck;
import xd.kagayakazee.aetherix.player.PlayerData;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


// не помню откуда я это спиздил
// вроде работает
public class HealthSpoof extends Check implements PacketCheck {
    
    private final Random random = new Random();
    private boolean enabled = true;
    

    private static final float MIN_SPOOFED_HP = 100.0f;
    private static final float MAX_SPOOFED_HP = 300.0f;
    
    public HealthSpoof(PlayerData playerData) {
        super(playerData, "HealthSpoof");
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!enabled) return;

        if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
            spoofEntityMetadata(event);
        }

        else if (event.getPacketType() == PacketType.Play.Server.UPDATE_ATTRIBUTES) {
            spoofUpdateAttributes(event);
        }

        else if (event.getPacketType() == PacketType.Play.Server.ENTITY_EFFECT) {
            spoofEntityEffect(event);
        }

        else if (event.getPacketType() == PacketType.Play.Server.SPAWN_PLAYER) {
            spoofSpawnPlayer(event);
        }
    }
    
    private void spoofEntityMetadata(PacketSendEvent event) {
        WrapperPlayServerEntityMetadata wrapper = new WrapperPlayServerEntityMetadata(event);
        int entityId = wrapper.getEntityId();

        if (event.getUser().getEntityId() == entityId) {
            return;
        }
        
        List<EntityData<?>> entityMetaData = wrapper.getEntityMetadata();
        boolean shouldModify = false;
        
        for (EntityData<?> data : entityMetaData) {
            if (data.getIndex() == MetadataIndex.HEALTH) {
                if (data.getValue() instanceof Float) {
                    float health = (Float) data.getValue();
                    if (health > 0.0f) {
                        float spoofedHealth = generateRandomHealth();
                        
                        @SuppressWarnings("unchecked")
                        EntityData<Float> floatData = (EntityData<Float>) data;
                        floatData.setValue(spoofedHealth);
                        shouldModify = true;
                    }
                }
            }

            else if (data.getIndex() == MetadataIndex.ABSORPTION) {
                if (data.getValue() instanceof Float) {
                    float absorption = (Float) data.getValue();
                    if (absorption >= 0.0f) {
                        float spoofedAbsorption = ThreadLocalRandom.current().nextFloat() * 50.0f;
                        
                        @SuppressWarnings("unchecked")
                        EntityData<Float> floatData = (EntityData<Float>) data;
                        floatData.setValue(spoofedAbsorption);
                        shouldModify = true;
                    }
                }
            }
        }
        
        if (shouldModify) {

            event.markForReEncode(true);
        }
    }
    
    private void spoofUpdateAttributes(PacketSendEvent event) {
        try {
            WrapperPlayServerUpdateAttributes wrapper = new WrapperPlayServerUpdateAttributes(event);
            int entityId = wrapper.getEntityId();
            
            if (event.getUser().getEntityId() == entityId) {
                return;
            }
            

            var attributes = wrapper.getProperties();
            boolean shouldModify = false;
            
            for (var attribute : attributes) {
                if (attribute.getAttribute().getName().equals("generic.max_health")) {

                    attribute.setValue(generateRandomHealth());
                    shouldModify = true;
                }
            }
            
            if (shouldModify) {

                event.markForReEncode(true);
            }
        } catch (Exception e) {

        }
    }
    
    private void spoofEntityEffect(PacketSendEvent event) {
        try {
            WrapperPlayServerEntityEffect wrapper = new WrapperPlayServerEntityEffect(event);
            int entityId = wrapper.getEntityId();
            
            if (event.getUser().getEntityId() == entityId) {
                return;
            }

            event.markForReEncode(true);
        } catch (Exception e) {
        }
    }
    
    private void spoofSpawnPlayer(PacketSendEvent event) {

    }
    
    private float generateRandomHealth() {
        return MIN_SPOOFED_HP + ThreadLocalRandom.current().nextFloat() * (MAX_SPOOFED_HP - MIN_SPOOFED_HP);
    }
    

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    

    public boolean isEnabled() {
        return enabled;
    }

    public static class MetadataIndex {
        public static final int HEALTH;
        public static final int ABSORPTION;
        
        static {
            ServerVersion serverVersion = PacketEvents.getAPI().getServerManager().getVersion();

            if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_17)) {
                HEALTH = 9;
                ABSORPTION = 15;
            } else if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_14)) {
                HEALTH = 8;
                ABSORPTION = 14;
            } else if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_10)) {
                HEALTH = 7;
                ABSORPTION = 11;
            } else {
                HEALTH = 6;
                ABSORPTION = 10;
            }
        }
    }
}
