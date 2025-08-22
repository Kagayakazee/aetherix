package xd.kagayakazee.aetherix.player.data;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class PacketStateData {
    private int hittiks = 0;
    private int usepearl = 0;
    private boolean slowedByUsingItem;
    private boolean isLastPacketWasServerRotation;
    private boolean isLastPacketWasOnePointSeventeenDuplicate;
}
