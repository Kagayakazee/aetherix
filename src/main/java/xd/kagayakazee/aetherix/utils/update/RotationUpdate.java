package xd.kagayakazee.aetherix.utils.update;

import lombok.Getter;
import org.bukkit.Location;


@Getter
public class RotationUpdate {
    private final Location from;
    private final Location to;
    private final float deltaYaw;
    private final float deltaPitch;
    private final float lastDeltaYaw;
    private final float lastDeltaPitch;

    public RotationUpdate(final Location from, final Location to, final float lastDeltaYaw, final float lastDeltaPitch) {
        this.from = from;
        this.to = to;
        this.deltaYaw = Math.abs(to.getYaw() - from.getYaw());
        this.deltaPitch = Math.abs(to.getPitch() - from.getPitch());
        this.lastDeltaYaw = lastDeltaYaw;
        this.lastDeltaPitch = lastDeltaPitch;
    }
}
