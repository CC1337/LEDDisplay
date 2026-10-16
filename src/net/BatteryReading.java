package net;

/** Optional fields appended to the existing NETIO response (indices 31-34). */
public final class BatteryReading {
    public final double soc;
    public final double power;

    private BatteryReading(double soc, double power) {
        this.soc = soc;
        this.power = power;
    }

    public static BatteryReading parse(String[] fields, long nowSeconds) {
        double soc = number(fields, 31);
        double power = number(fields, 32);
        if (soc < 0 || soc > 100) soc = Double.NaN;
        // SOC may remain unchanged for hours. Live power confirms the source is alive.
        if (!Double.isFinite(power) || !fresh(fields, 34, nowSeconds)) {
            soc = Double.NaN;
            power = Double.NaN;
        }
        return new BatteryReading(soc, power);
    }

    private static double number(String[] fields, int index) {
        try {
            double value = Double.parseDouble(fields[index].trim());
            return Double.isFinite(value) ? value : Double.NaN;
        } catch (IndexOutOfBoundsException | NumberFormatException exception) {
            return Double.NaN;
        }
    }

    private static boolean fresh(String[] fields, int index, long now) {
        double timestamp = number(fields, index);
        // Allow small clock differences, but never disguise old source data as live.
        return Double.isFinite(timestamp) && timestamp > 0 && timestamp <= now + 30
                && now - timestamp <= 12 * 60 * 60;
    }
}
