package effects.info;

import effects.IEffect;
import led.ILEDArray;
import net.BatteryReading;
import net.NetIO1OG;

/** One row; the power overlay is clipped to the actual state-of-charge fill. */
public class BatteryBarEffect implements IEffect {
    private int x;
    private int y;

    public BatteryBarEffect(int x, int y) { this.x = x; this.y = y; }

    @Override
    public void apply(ILEDArray leds) {
        draw(leds, NetIO1OG.getInstance().getBatteryReading(), x, y);
    }

    public static void draw(ILEDArray leds, BatteryReading reading, int x, int y) {
        if (!Double.isFinite(reading.soc)) return;
        int width = leds.sizeX() - x;
        double filled = width * reading.soc / 100.0;
        double powerPixels = Double.isFinite(reading.power) && Math.abs(reading.power) >= 100
                ? Math.min(filled, Math.abs(reading.power) / 500.0) : 0;
        for (int i = 0; i < width; i++) {
            double coverage = overlap(i, i + 1, 0, filled);
            double overlay = reading.power > 0
                    ? overlap(i, i + 1, filled - powerPixels, filled)
                    : overlap(i, i + 1, 0, powerPixels);
            // Dim turquoise fill, green charge at right, orange discharge at left.
            double base = coverage - overlay;
            int r = (int) Math.round(8 * base + (reading.power > 0 ? 20 : 120) * overlay);
            int g = (int) Math.round(55 * base + (reading.power > 0 ? 110 : 40) * overlay);
            int b = (int) Math.round(65 * base + 5 * overlay);
            leds.setLed(x + i, y, r, g, b);
        }
    }

    private static double overlap(double a, double b, double c, double d) {
        return Math.max(0, Math.min(b, d) - Math.max(a, c));
    }

    @Override public void setPosX(int x) { this.x = x; }
    @Override public void setPosY(int y) { this.y = y; }
    @Override public int getPosX() { return x; }
    @Override public int getPosY() { return y; }
}
