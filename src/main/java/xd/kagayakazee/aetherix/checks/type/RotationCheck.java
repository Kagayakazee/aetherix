package xd.kagayakazee.aetherix.checks.type;

import xd.kagayakazee.aetherix.player.PlayerData;
import xd.kagayakazee.aetherix.utils.update.RotationUpdate;

public interface RotationCheck {
    void onRotation(final PlayerData data, final RotationUpdate update);
}
