package xd.kagayakazee.aetherix.processors;

import java.util.ArrayDeque;


public class RotationProcessor {

    private float yaw;
    private float pitch;
    private float lastYaw;
    private float lastPitch;


    private float deltaYaw;
    private float deltaPitch;

    private float lastDeltaYaw;
    private float lastDeltaPitch;


    private float yawAccel;
    private float pitchAccel;

    private float lastYawAccel;
    private float lastPitchAccel;

    private float rawMouseDeltaX;
    private float rawMouseDeltaY;
    private float fuckedPredictedPitch;
    private float fuckedPredictedYaw;
    private float lastFuckedPredictedPitch;
    private float lastFuckedPredictedYaw;
    private boolean invalidRate;
    private boolean invalidSensitivity;
    private boolean cinematic;
    private double finalSensitivity;
    private double mcpSensitivity;
    private final ArrayDeque<Integer> sensitivitySamples = new ArrayDeque<>();
    private int sensitivity;
    private int lastRate;
    private int lastInvalidSensitivity;
    private int lastCinematic;
    private int mouseDeltaX;
    private int mouseDeltaY;


    // private float lastjoltYaw;
    // private float joltYaw;
    // private float joltPitch;


    public RotationProcessor() {

    }

    public void handle(float yaw, float pitch) {
        this.lastYaw = this.yaw;
        this.lastPitch = this.pitch;
        this.yaw = yaw;
        this.pitch = pitch;


        this.lastDeltaYaw = this.deltaYaw;
        this.lastDeltaPitch = this.deltaPitch;


        this.deltaYaw = Math.abs(yaw - this.lastYaw);
        this.deltaPitch = Math.abs(pitch - this.lastPitch);


        this.lastYawAccel = this.yawAccel;
        this.lastPitchAccel = this.pitchAccel;


        this.yawAccel = Math.abs(this.deltaYaw - this.lastDeltaYaw);
        this.pitchAccel = Math.abs(this.deltaPitch - this.lastDeltaPitch);


        // this.lastjoltYaw = this.joltYaw;
        // this.joltYaw = Math.abs(this.deltaYaw - this.lastDeltaYaw);
        // this.joltPitch = Math.abs(this.deltaPitch - this.lastDeltaPitch);

        float f = (float) this.mcpSensitivity * 0.6f + 0.2f;
        float gcd = f * f * f * 1.2f;


        this.rawMouseDeltaX = this.deltaYaw / gcd;
        this.rawMouseDeltaY = this.deltaPitch / gcd;
        this.mouseDeltaX = (int) (this.deltaYaw / gcd);
        this.mouseDeltaY = (int) (this.deltaPitch / gcd);

        this.processCinematic();

        float var3 = 0.512f;
        float var4 = 1.073742f;

        float expectedYaw = this.deltaYaw * 1.073742f + (float) ((double) this.deltaYaw + 0.15);
        float expectedPitch = this.deltaPitch * 1.073742f - (float) ((double) this.deltaPitch - 0.15);
        float pitchDiff = Math.abs(this.deltaPitch - expectedPitch);
        float yawDiff = Math.abs(this.deltaYaw - expectedYaw);

        this.lastFuckedPredictedPitch = this.fuckedPredictedPitch;
        this.lastFuckedPredictedYaw = this.fuckedPredictedYaw;

        this.fuckedPredictedPitch = Math.abs(this.deltaPitch - pitchDiff);
        this.fuckedPredictedYaw = Math.abs(this.deltaYaw - yawDiff);

        if ((double) this.deltaPitch > 0.1 && this.deltaPitch < 25.0f) {

        }
    }

    public boolean hasValidSensitivity() {
        return this.sensitivity > 0 && this.sensitivity < 200;
    }

    private void processCinematic() {

        float differenceYaw = Math.abs(this.deltaYaw - this.lastDeltaYaw);
        float differencePitch = Math.abs(this.deltaPitch - this.lastDeltaPitch);
        float joltYaw = Math.abs(differenceYaw - this.deltaYaw);
        float joltPitch = Math.abs(differencePitch - this.deltaPitch);
        if (!(this.deltaPitch < 20.0f) || this.finalSensitivity < 0.0) {
            // empty if block
        }
        if (!this.invalidRate || this.invalidSensitivity) {
            // empty if block
        }
    }

    public boolean hasTooLowSensitivity() {
        return this.sensitivity >= 0 && this.sensitivity < 50;
    }


    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getLastYaw() {
        return this.lastYaw;
    }

    public float getLastPitch() {
        return this.lastPitch;
    }

    public float getDeltaYaw() {
        return this.deltaYaw;
    }

    public float getDeltaPitch() {
        return this.deltaPitch;
    }

    public float getLastDeltaYaw() {
        return this.lastDeltaYaw;
    }

    public float getLastDeltaPitch() {
        return this.lastDeltaPitch;
    }

    public float getYawAccel() {
        return this.yawAccel;
    }

    public float getPitchAccel() {
        return this.pitchAccel;
    }

    public float getLastYawAccel() {
        return this.lastYawAccel;
    }

    public float getLastPitchAccel() {
        return this.lastPitchAccel;
    }

    public float getRawMouseDeltaX() {
        return this.rawMouseDeltaX;
    }

    public float getRawMouseDeltaY() {
        return this.rawMouseDeltaY;
    }

    public float getFuckedPredictedPitch() {
        return this.fuckedPredictedPitch;
    }

    public float getFuckedPredictedYaw() {
        return this.fuckedPredictedYaw;
    }

    public float getLastFuckedPredictedPitch() {
        return this.lastFuckedPredictedPitch;
    }

    public float getLastFuckedPredictedYaw() {
        return this.lastFuckedPredictedYaw;
    }

    public boolean isInvalidRate() {
        return this.invalidRate;
    }

    public boolean isInvalidSensitivity() {
        return this.invalidSensitivity;
    }

    public boolean isCinematic() {
        return this.cinematic;
    }

    public double getFinalSensitivity() {
        return this.finalSensitivity;
    }

    public double getMcpSensitivity() {
        return this.mcpSensitivity;
    }

    public ArrayDeque<Integer> getSensitivitySamples() {
        return this.sensitivitySamples;
    }

    public int getSensitivity() {
        return this.sensitivity;
    }

    public int getLastRate() {
        return this.lastRate;
    }

    public int getLastInvalidSensitivity() {
        return this.lastInvalidSensitivity;
    }

    public int getLastCinematic() {
        return this.lastCinematic;
    }

    public int getMouseDeltaX() {
        return this.mouseDeltaX;
    }

    public int getMouseDeltaY() {
        return this.mouseDeltaY;
    }

    // public float getLastjoltYaw() {
    //     return this.lastjoltYaw;
    // }
    //
    // public float getJoltYaw() {
    //     return this.joltYaw;
    // }
    //
    // public float getJoltPitch() {
    //     return this.joltPitch;
    // }
}