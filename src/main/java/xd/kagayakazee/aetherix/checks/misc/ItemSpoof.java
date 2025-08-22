package xd.kagayakazee.aetherix.checks.misc;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;

import xd.kagayakazee.aetherix.checks.Check;
import xd.kagayakazee.aetherix.checks.type.PacketCheck;
import xd.kagayakazee.aetherix.player.PlayerData;

import java.util.List;


public class ItemSpoof extends Check implements PacketCheck {

    private boolean enabled = true;
    private final int FAKE_AMOUNT = 1;
    private final int FAKE_DAMAGE = Short.MAX_VALUE;
    private static final int MAIN_HAND_ITEM_INDEX = 8;

    public ItemSpoof(PlayerData playerData) {
        super(playerData, "ItemSpoof");
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!enabled) return;

        if (event.getPacketType() == PacketType.Play.Server.ENTITY_EQUIPMENT) {
            handleEntityEquipment(event);
        } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
            handleEntityMetadata(event);
        }
    }

    private void handleEntityEquipment(PacketSendEvent event) {
        try {
            WrapperPlayServerEntityEquipment wrapper = new WrapperPlayServerEntityEquipment(event);
            if (event.getUser().getEntityId() == wrapper.getEntityId()) {
                return;
            }
            List<Equipment> originalEquipment = wrapper.getEquipment();
            boolean modified = false;

            for (Equipment equipmentEntry : originalEquipment) {
                ItemStack stack = equipmentEntry.getItem();
                boolean isHandSlot = equipmentEntry.getSlot() == EquipmentSlot.MAIN_HAND || 
                                   equipmentEntry.getSlot() == EquipmentSlot.OFF_HAND;
                if (stack != null && stack.getType() != ItemTypes.AIR && !isHandSlot) {
                    ItemStack fakeStack = createFakeStack(stack);
                    equipmentEntry.setItem(fakeStack);
                    modified = true;
                }
            }

            if (modified) {
                event.markForReEncode(true);
            }
        } catch (Exception e) {
        }
    }

    private void handleEntityMetadata(PacketSendEvent event) {
        try {
            WrapperPlayServerEntityMetadata wrapper = new WrapperPlayServerEntityMetadata(event);
            if (event.getUser().getEntityId() == wrapper.getEntityId()) {
                return;
            }

            List<EntityData<?>> originalMetadata = wrapper.getEntityMetadata();
            boolean modified = false;

            for (EntityData<?> data : originalMetadata) {
                if (data.getIndex() == MAIN_HAND_ITEM_INDEX && data.getValue() instanceof ItemStack) {
                    ItemStack originalStack = (ItemStack) data.getValue();
                    if (originalStack != null && originalStack.getType() != ItemTypes.AIR) {
                        ItemStack fakeStack = createFakeStack(originalStack);
                        @SuppressWarnings("unchecked")
                        EntityData<ItemStack> itemData = (EntityData<ItemStack>) data;
                        itemData.setValue(fakeStack);
                        modified = true;
                        break;
                    }
                }
            }

            if (modified) {
                event.markForReEncode(true);
            }
        } catch (Exception e) {
        }
    }

    private ItemStack createFakeStack(ItemStack original) {
        try {
            ItemStack.Builder builder = ItemStack.builder()
                    .type(original.getType())
                    .amount(FAKE_AMOUNT);
            ItemStack fakeStack = builder.build();
            fakeStack.setDamageValue(FAKE_DAMAGE);
            return fakeStack;
        } catch (Exception e) {
            return original;
        }
    }


    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    public boolean isEnabled() {
        return enabled;
    }
}
