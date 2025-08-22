package xd.kagayakazee.aetherix.utils;


import java.util.List;


public final class Matan {
    private Matan() {
    }

    public static final double MINIMUM_DIVISOR = ((Math.pow(0.2, 3) * 8) * 0.15) - 1e-3;
    private static final float DEGREES_TO_RADIANS = (float) Math.PI / 180f;

    public static double gcd(double a, double b) {
        if (a == 0) return 0;

        if (a < b) {
            double temp = a;
            a = b;
            b = temp;
        }

        while (b > MINIMUM_DIVISOR) {
            double temp = a - (Math.floor(a / b) * b);
            a = b;
            b = temp;
        }
        return a;
    }

    public static double calculateSD(List<Double> numbers) {
        double sum = 0.0;
        double standardDeviation = 0.0;
        for (double rotation : numbers) {
            sum += rotation;
        }
        double mean = sum / numbers.size();
        for (double num : numbers) {
            standardDeviation += Math.pow(num - mean, 2);
        }
        return Math.sqrt(standardDeviation / numbers.size());
    }

    public static int floor(double d) {
        return (int) Math.floor(d);
    }

    public static int ceil(double d) {
        return (int) Math.ceil(d);
    }

    public static double clamp(double num, double min, double max) {
        return Math.max(min, Math.min(num, max));
    }

    public static int clamp(int num, int min, int max) {
        return Math.max(min, Math.min(num, max));
    }

    public static float clamp(float num, float min, float max) {
        return Math.max(min, Math.min(num, max));
    }

    public static double lerp(double lerpAmount, double start, double end) {
        return start + lerpAmount * (end - start);
    }

    public static double frac(double p_14186_) {
        return p_14186_ - lfloor(p_14186_);
    }

    public static long lfloor(double p_14135_) {
        long i = (long) p_14135_;
        return p_14135_ < (double) i ? i - 1L : i;
    }

    public static int sign(double x) {
        if (x == 0.0) {
            return 0;
        }
        return x > 0.0 ? 1 : -1;
    }

    public static float square(float value) {
        return value * value;
    }

    public static double distanceToHorizontalCollision(double position) {
        return Math.min(Math.abs(position % (1 / 640d)), Math.abs(Math.abs(position % (1 / 640d)) - (1 / 640d)));
    }

    public static boolean betweenRange(double value, double min, double max) {
        return value > min && value < max;
    }

    public static boolean inRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    public static boolean inRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    public static boolean isNearlySame(double a, double b, double epoch) {
        return Math.abs(a - b) < epoch;
    }

    public static long hashCode(double x, int y, double z) {
        long l = (long) (x * 3129871) ^ (long) z * 116129781L ^ (long) y;
        l = l * l * 42317861L + l * 11L;
        return l >> 16;
    }

    public static float radians(float degrees) {
        return degrees * DEGREES_TO_RADIANS;
    }

    public static int getSectionCoord(int coord) {
        return coord >> 4;
    }

    public static int getSectionCoord(double coord) {
        return getSectionCoord(Matan.floor(coord));
    }

    public static long asLong(int x, int y, int z) {
        long l = 0L;
        l |= ((long) x & 4194303L) << 42;
        l |= ((long) y & 1048575L);
        l |= ((long) z & 4194303L) << 20;
        return l;
    }

    public static int unpackX(long packed) {
        return (int) (packed << 0 >> 42);
    }

    public static int unpackY(long packed) {
        return (int) (packed << 44 >> 44);
    }

    public static int unpackZ(long packed) {
        return (int) (packed << 22 >> 42);
    }

    public static float pitchDiff(float pitch1, float pitch2) {
        return pitch2 - pitch1;
    }
}