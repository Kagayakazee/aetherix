package xd.kagayakazee.aetherix.player;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import xd.kagayakazee.aetherix.checks.CheckManager;

import xd.kagayakazee.aetherix.player.data.PacketStateData;
import xd.kagayakazee.aetherix.processors.AimProcessor;
import xd.kagayakazee.aetherix.processors.RotationProcessor;

import java.util.UUID;






@Getter
public class PlayerData {
    public final Player player;
    public final UUID uuid;
    @Setter
    private double aiProbability;
    @Setter
    @Getter
    private boolean attackedThisTick;



    @Setter
    private double x, y, z;
    @Setter
    private float yaw, pitch;
    private double lastX, lastY, lastZ;
    private float lastYaw, lastPitch;
    @Setter
    private boolean onGround, lastOnGround;
    private boolean isSprinting, isSneaking, lastSprinting, wasSneaking;
    @Setter
    private double fallDistance;
    private Vector actualMovement = new Vector(0, 0, 0);
    @Setter
    private int usingItemBuffer;
    @Setter
    private ItemStack heldItem;
    @Setter
    private int airTicks;

    public final PacketStateData packetStateData;


    public final RotationProcessor rotationProcessor;
    public final AimProcessor aimProcessor;


    public final CheckManager checkManager;
    @Setter
    private int slowdownGraceTicks;


    @Setter
    @Getter
    private boolean receiveAlerts = true;


    private boolean isSwimming;
    private boolean inLiquid;
    private boolean nearBubbleColumn;

    private long lastHitTime;
    private long lastTeleportTime;
    private int ticksSinceServerPosAdjust;
    private double horizontalSpeed;
    private double lastHorizontalSpeed;
    private double currentFriction = 0.91;
    private double currentSpeedAttribute = 0.1;
    private float jumpHeight = 0.42f;
    private boolean isCollidingHorizontally = false;
    private boolean isCollidingVertically = false;
    private boolean wasOnGroundLastTick_ForPhysics = false;
    @Setter
    private int ticksInAir;
    private double y_before_last_tick;
    private boolean isNearClimbable;
    private Vector clientVelocity = new Vector(0, 0, 0);
    private Vector lastClientVelocity = new Vector(0, 0, 0);
    private long lastSetbackTime;
    public PlayerData(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
        this.airTicks = 0;
        this.lastX = this.x;
        this.slowdownGraceTicks = 0;
        this.lastTeleportTime = 0;
        this.ticksSinceServerPosAdjust = 100;
        this.rotationProcessor = new RotationProcessor();
        this.aimProcessor = new AimProcessor();
        this.packetStateData = new PacketStateData();

        this.aiProbability = 0.0;
        this.attackedThisTick = false;

        this.heldItem = null;

        this.checkManager = new CheckManager(this);

        this.x = player.getLocation().getX();
        this.y = player.getLocation().getY();
        this.z = player.getLocation().getZ();
        this.yaw = player.getLocation().getYaw();
        this.pitch = player.getLocation().getPitch();
        this.onGround = player.isOnGround();
        this.usingItemBuffer = 0;
        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;
        this.lastYaw = this.yaw;
        this.lastPitch = this.pitch;
        this.lastOnGround = this.onGround;


    }
    public int getTicksSinceServerPosAdjust() {
        return this.ticksSinceServerPosAdjust;
    }

    public void resetTicksSinceServerPosAdjust() {
        this.ticksSinceServerPosAdjust = 0;
    }

    public void incrementTicksSinceServerPosAdjust() {
        if (this.ticksSinceServerPosAdjust < 200) {
            this.ticksSinceServerPosAdjust++;
        }
    }



    public void setLastHitTime() {
        this.lastHitTime = System.currentTimeMillis();
    }
    @Setter @Getter
    private boolean isBeingSetbacked;


    public void setLastTeleportTime() {
        this.lastTeleportTime = System.currentTimeMillis();
    }



    public void updateHorizontalSpeed() {
        this.lastHorizontalSpeed = this.horizontalSpeed;
        this.horizontalSpeed = Math.hypot(this.getActualMovement().getX(), this.getActualMovement().getZ());
    }


    public void updateEnvironmentState() {
        Location loc = player.getLocation();
        Material headBlock = player.getEyeLocation().getBlock().getType();
        Material feetBlock = loc.getBlock().getType();
        this.inLiquid = isLiquid(headBlock) || isLiquid(feetBlock);

        this.nearBubbleColumn = feetBlock == Material.BUBBLE_COLUMN ||
                loc.clone().subtract(0, 1, 0).getBlock().getType() == Material.BUBBLE_COLUMN;
    }

    private boolean isLiquid(Material material) {
        return material == Material.WATER || material == Material.WATER || material == Material.LAVA || material == Material.LAVA;
    }

    public boolean isInLiquid() {
        return this.inLiquid;
    }


    public boolean isNearBubbleColumn() {
        return this.nearBubbleColumn;
    }


    public boolean wasRecentlyHit() {
        return System.currentTimeMillis() - lastHitTime < 1500;
    }


    public boolean wasRecentlyTeleported() {
        return System.currentTimeMillis() - lastTeleportTime < 1000;
    }
    public boolean isUsingItem() {
        if (this.heldItem == null) {
            return false;
        }

        org.bukkit.inventory.ItemStack bukkitStack = SpigotConversionUtil.toBukkitItemStack(this.heldItem);
        if (bukkitStack == null) {
            return false;
        }

        Material material = bukkitStack.getType();
        if (material == null) {
            return false;
        }

        if (material.isEdible()) {
            return true;
        }

        switch (material.name()) {
            case "BOW":
            case "CROSSBOW":
            case "TRIDENT":
            case "SHIELD":
            case "POTION":
            case "MILK_BUCKET":
            case "SPYGLASS":
                return true;
            default:
                return false;
        }
    }
    public void updateRotation(float newYaw, float newPitch) {
        this.lastYaw = this.yaw;
        this.lastPitch = this.pitch;
        this.yaw = newYaw;
        this.pitch = newPitch;
    }
    public void setY_before_last_tick(double y) {
        this.y_before_last_tick = y;
    }
    public void updatePosition(double x, double y, double z, boolean onGround) {
        this.y_before_last_tick = this.lastY;

        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;
        this.x = x;
        this.y = y;
        this.z = z;

        this.actualMovement = new Vector(this.x - this.lastX, this.y - this.lastY, this.z - this.lastZ);

        this.lastOnGround = this.onGround;
        this.onGround = onGround;
//        if (onGround) {
//            this.lastGroundLocation = new Location(player.getWorld(), x, y, z, getYaw(), getPitch());
//        }

        if (this.onGround) {
            this.ticksInAir = 0;
        } else {
            this.ticksInAir++;
        }
    }
//    public Location getLastGroundLocation() {
//        return lastGroundLocation;
//    }
    public void updateClimbableState() {
        Block blockAtFeet = player.getLocation().getBlock();
        Block blockAtHead = player.getEyeLocation().getBlock();
        this.isNearClimbable = isClimbable(blockAtFeet.getType()) || isClimbable(blockAtHead.getType());
    }

    private boolean isClimbable(Material material) {
        if (material == null) return false;
        return material == Material.LADDER || material == Material.VINE || material.name().equals("TWISTING_VINES") || material.name().equals("WEEPING_VINES");
    }

    public void updateLastClientVelocity() {
        this.lastClientVelocity = this.getActualMovement().clone();
    }
    public void updateStates(boolean isSprinting, boolean isSneaking) {
        this.lastSprinting = this.isSprinting;
        this.wasSneaking = this.isSneaking;
        this.isSprinting = isSprinting;
        this.isSneaking = isSneaking;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getLastYaw() {
        return lastYaw;
    }

    public float getLastPitch() {
        return lastPitch;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public boolean isSprinting() {
        return isSprinting;
    }

    public boolean isSneaking() {
        return isSneaking;
    }

    public double getFallDistance() {
        return fallDistance;
    }

    public Vector getActualMovement() {
        return actualMovement;
    }

    public RotationProcessor getRotationProcessor() {
        return rotationProcessor;
    }


    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public boolean isAttackedThisTick() {
        return attackedThisTick;
    }



    public ItemStack getHeldItem() {
        return heldItem;
    }

    public PacketStateData getPacketStateData() {
        return packetStateData;
    }

    public AimProcessor getAimProcessor() {
        return aimProcessor;
    }

    public CheckManager getCheckManager() {
        return checkManager;
    }


}