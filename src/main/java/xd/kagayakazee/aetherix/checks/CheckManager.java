package xd.kagayakazee.aetherix.checks;

import lombok.Getter;
import xd.kagayakazee.aetherix.checks.combat.*;

//import xd.kagayakazee.gulac.checks.movement.NoSlowA;
import xd.kagayakazee.aetherix.checks.misc.HealthSpoof;
import xd.kagayakazee.aetherix.checks.misc.ItemSpoof;
//import xd.kagayakazee.gulac.checks.movement.NoSlow;
//import xd.kagayakazee.gulac.checks.movement.Spider;
import xd.kagayakazee.aetherix.checks.type.MovementCheck;
import xd.kagayakazee.aetherix.checks.type.PacketCheck;
import xd.kagayakazee.aetherix.checks.type.RotationCheck;
import xd.kagayakazee.aetherix.player.PlayerData;

import java.util.ArrayList;
import java.util.List;


@Getter
public class CheckManager {

    private final List<Check> checks = new ArrayList<>();
    private final List<PacketCheck> packetChecks = new ArrayList<>();
    private final List<RotationCheck> rotationChecks = new ArrayList<>();
    private final List<MovementCheck> movementChecks = new ArrayList<>();

    public CheckManager(final PlayerData data) {

     //   register(new AutoSwap(data));
//        register(new InvMove(data));
//        register(new AuraSprint(data));
        register(new AICheck(data));
        register(new DataCollectorCheck(data));
        register(new HealthSpoof(data));
        register(new ItemSpoof(data));

    }

    private void register(final Check check) {
        this.checks.add(check);

        if (check instanceof PacketCheck) {
            this.packetChecks.add((PacketCheck) check);
        }
        if (check instanceof RotationCheck) {
            this.rotationChecks.add((RotationCheck) check);
        }
        if (check instanceof MovementCheck) {
            this.movementChecks.add((MovementCheck) check);
        }
    }

    public <T extends Check> T getCheck(Class<T> clazz) {
        for (Check check : checks) {
            if (check.getClass() == clazz) {
                return (T) check;
            }
        }
        return null;
    }
    public List<MovementCheck> getMovementChecks() {
        return movementChecks;
    }
}