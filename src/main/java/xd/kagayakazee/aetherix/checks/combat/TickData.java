package xd.kagayakazee.aetherix.checks.combat;

import xd.kagayakazee.aetherix.player.PlayerData;
import xd.kagayakazee.aetherix.processors.AimProcessor;
import xd.kagayakazee.aetherix.processors.RotationProcessor;

import java.util.Locale;
import java.util.StringJoiner;



public class TickData {
    final float deltaYaw, deltaPitch;
    final float accelYaw, accelPitch;
    final float jerkYaw, jerkPitch;
    final float gcdErrorYaw, gcdErrorPitch;
    final int isOnGround, isSprinting, isSneaking, isUsingItem;
    final double playerSpeedHorizontal, playerFallDistance;
    final int isAttacking, ticksSinceAttack;

    public TickData(PlayerData player, boolean attackPacketSentThisTick) {
        RotationProcessor rotationProcessor = player.getRotationProcessor();
        AimProcessor aimProcessor = player.getAimProcessor();
        this.deltaYaw = player.getYaw() - player.getLastYaw();
        this.deltaPitch = player.getPitch() - player.getLastPitch();

        this.accelYaw = rotationProcessor.getYawAccel();
        this.accelPitch = rotationProcessor.getPitchAccel();
        this.jerkYaw = this.accelYaw - rotationProcessor.getLastYawAccel();
        this.jerkPitch = this.accelPitch - rotationProcessor.getLastPitchAccel();

        if (aimProcessor != null && aimProcessor.modeX > 0) {
            double errorPitch = Math.abs(this.deltaPitch % aimProcessor.modeX);
            this.gcdErrorPitch = (float) Math.min(errorPitch, aimProcessor.modeX - errorPitch);
        } else {
            this.gcdErrorPitch = 0;
        }
        if (aimProcessor != null && aimProcessor.modeY > 0) {
            double errorYaw = Math.abs(this.deltaYaw % aimProcessor.modeY);
            this.gcdErrorYaw = (float) Math.min(errorYaw, aimProcessor.modeY - errorYaw);
        } else {
            this.gcdErrorYaw = 0;
        }

        this.isOnGround = player.isOnGround() ? 1 : 0;
        this.isSprinting = player.isSprinting() ? 1 : 0;
        this.isSneaking = player.isSneaking() ? 1 : 0;
        this.isUsingItem = player.getPacketStateData().isSlowedByUsingItem() ? 1 : 0;
        this.playerSpeedHorizontal = Math.sqrt(Math.pow(player.getActualMovement().getX(), 2) + Math.pow(player.getActualMovement().getZ(), 2));
        this.playerFallDistance = player.getFallDistance();
        this.isAttacking = attackPacketSentThisTick ? 1 : 0;
        this.ticksSinceAttack = player.getPacketStateData().getHittiks();
    }

    public static String getCsvHeader() {
        return "is_cheating,delta_yaw,delta_pitch,accel_yaw,accel_pitch,jerk_yaw,jerk_pitch,"
                + "gcd_error_yaw,gcd_error_pitch,is_on_ground,is_sprinting,is_sneaking,is_using_item,"
                + "player_speed_horizontal,player_fall_distance,is_attacking,ticks_since_attack";
    }

    public String toCsvRow(String status) {
        int cheatingStatus = status.equalsIgnoreCase("CHEAT") ? 1 : 0;

        StringJoiner joiner = new StringJoiner(",");

        joiner.add(String.valueOf(cheatingStatus));
        joiner.add(String.format(Locale.US, "%.6f", deltaYaw));
        joiner.add(String.format(Locale.US, "%.6f", deltaPitch));
        joiner.add(String.format(Locale.US, "%.6f", accelYaw));
        joiner.add(String.format(Locale.US, "%.6f", accelPitch));
        joiner.add(String.format(Locale.US, "%.6f", jerkYaw));
        joiner.add(String.format(Locale.US, "%.6f", jerkPitch));
        joiner.add(String.format(Locale.US, "%.6f", gcdErrorYaw));
        joiner.add(String.format(Locale.US, "%.6f", gcdErrorPitch));
        joiner.add(String.valueOf(isOnGround));
        joiner.add(String.valueOf(isSprinting));
        joiner.add(String.valueOf(isSneaking));
        joiner.add(String.valueOf(isUsingItem));
        joiner.add(String.format(Locale.US, "%.6f", playerSpeedHorizontal));
        joiner.add(String.format(Locale.US, "%.6f", playerFallDistance));
        joiner.add(String.valueOf(isAttacking));
        joiner.add(String.valueOf(ticksSinceAttack));

        return joiner.toString();
    }
}