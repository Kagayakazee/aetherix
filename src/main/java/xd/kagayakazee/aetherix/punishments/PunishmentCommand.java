package xd.kagayakazee.aetherix.punishments;


public class PunishmentCommand {
    public final int threshold;
    public final int interval;
    public final String command;

    public PunishmentCommand(int threshold, int interval, String command) {
        this.threshold = threshold;
        this.interval = interval;
        this.command = command;
    }

    /**
     * Проверяет, должна ли эта команда выполниться для данного количества нарушений.
     */
    public boolean shouldExecute(int vl) {
        if (vl < threshold) {
            return false; // Порог не достигнут
        }
        if (interval == 0) {
            return vl == threshold; // Выполнить ровно один раз на пороге
        }
        // Выполнить на пороге и на каждом интервале после него
        return (vl - threshold) % interval == 0;
    }
}