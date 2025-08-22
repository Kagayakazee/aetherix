package xd.kagayakazee.aetherix.processors;

import xd.kagayakazee.aetherix.utils.Matan;
import xd.kagayakazee.aetherix.utils.RunningMode;
import xd.kagayakazee.aetherix.utils.Pair;




public class AimProcessor {

    private static final int SIGNIFICANT_SAMPLES_THRESHOLD = 15;
    private static final int TOTAL_SAMPLES_THRESHOLD = 80;

    public AimProcessor() {

    }

    private final RunningMode xRotMode = new RunningMode(TOTAL_SAMPLES_THRESHOLD);
    private final RunningMode yRotMode = new RunningMode(TOTAL_SAMPLES_THRESHOLD);

    private float lastXRot;
    private float lastYRot;

    public double sensitivityX;
    public double sensitivityY;
    public double divisorX;
    public double divisorY;
    public double modeX;
    public double modeY;
    public double deltaDotsX;
    public double deltaDotsY;


    public void handle(final float deltaPitchAbs, final float deltaYawAbs) {

        this.divisorX = Matan.gcd(deltaPitchAbs, lastXRot);
        if (deltaPitchAbs > 0.0 && deltaPitchAbs < 5.0 && divisorX > Matan.MINIMUM_DIVISOR) {
            this.xRotMode.add(divisorX);
            this.lastXRot = deltaPitchAbs;
        }


        this.divisorY = Matan.gcd(deltaYawAbs, lastYRot);
        if (deltaYawAbs > 0.0 && deltaYawAbs < 5.0 && divisorY > Matan.MINIMUM_DIVISOR) {
            this.yRotMode.add(divisorY);
            this.lastYRot = deltaYawAbs;
        }


        if (this.xRotMode.size() > SIGNIFICANT_SAMPLES_THRESHOLD) {
            Pair<Double, Integer> modeResultX = this.xRotMode.getMode();
            if (modeResultX != null && modeResultX.second() > SIGNIFICANT_SAMPLES_THRESHOLD) {
                this.modeX = modeResultX.first();
                this.sensitivityX = convertToSensitivity(this.modeX);
            }
        }


        if (this.yRotMode.size() > SIGNIFICANT_SAMPLES_THRESHOLD) {
            Pair<Double, Integer> modeResultY = this.yRotMode.getMode();
            if (modeResultY != null && modeResultY.second() > SIGNIFICANT_SAMPLES_THRESHOLD) {
                this.modeY = modeResultY.first();
                this.sensitivityY = convertToSensitivity(this.modeY);
            }
        }

        if (this.modeX > 0) {
            this.deltaDotsX = deltaPitchAbs / this.modeX;
        }
        if (this.modeY > 0) {
            this.deltaDotsY = deltaYawAbs / this.modeY;
        }
    }


    public static double convertToSensitivity(double gcdValue) {
        double step1 = gcdValue / 0.15D / 8.0D;
        double step2 = Math.cbrt(step1);
        return (step2 - 0.2D) / 0.6D;
    }
}