import effects.info.BatteryBarEffect;
import led.ILEDArray;
import led.ILED;
import effects.IEffect;
import net.BatteryReading;
import net.NetIO1OG;
import net.PvData;
import helper.FileHelper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/** Run in an isolated working directory: writes a mock global.properties. */
public class BatteryFeatureTest {
    private static final long NOW = 1800000000L;

    // Test framebuffer deliberately avoids LED's GPIO/brightness initialization.
    private static class Pixel {
        int red, green, blue;
        int r() { return red; }
        int g() { return green; }
        int b() { return blue; }
    }

    private static class Frame implements ILEDArray {
        Pixel[][] pixels = new Pixel[60][16];
        Frame() { reset(); }
        public int sizeX() { return 60; }
        public int sizeY() { return 16; }
        Pixel pixel(int x, int y) { return pixels[x][y]; }
        public ILED led(int x, int y) { throw new UnsupportedOperationException(); }
        public void setLed(int x, int y, int r, int g, int b) {
            Pixel p = pixels[x][y]; p.red = r; p.green = g; p.blue = b;
        }
        public void blendLed(int x, int y, int r, int g, int b, double a) {
            throw new UnsupportedOperationException();
        }
        public void reset() {
            for (int x = 0; x < 60; x++) for (int y = 0; y < 16; y++) pixels[x][y] = new Pixel();
        }
        public void applyEffect(IEffect effect) { effect.apply(this); }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static String[] fields(String soc, String power) {
        String[] result = new String[35];
        Arrays.fill(result, "");
        result[0] = "21.5";
        result[21] = "1";
        result[31] = soc;
        result[32] = power;
        result[33] = result[34] = String.valueOf(NOW);
        return result;
    }

    private static void render() {
        Frame leds = new Frame();
        BatteryBarEffect.draw(leds, BatteryReading.parse(fields("60", "-2400"), NOW), 0, 15);
        check(leds.pixel(0, 15).r() == 120, "discharge begins at left");
        check(leds.pixel(4, 15).r() > 8, "partial fifth power pixel");
        check(leds.pixel(5, 15).r() == 8, "power limited to 4.8 pixels");
        check(leds.pixel(35, 15).b() == 65 && leds.pixel(36, 15).b() == 0, "60 percent = 36 pixels");
        check(leds.pixel(0, 14).r() == 0, "text row untouched");
        leds.reset();
        BatteryBarEffect.draw(leds, BatteryReading.parse(fields("60", "2000"), NOW), 0, 15);
        check(leds.pixel(31, 15).g() == 55 && leds.pixel(32, 15).g() == 110, "charge at right inside fill");
        check(leds.pixel(36, 15).g() == 0, "charge never extends fill");
        leds.reset();
        BatteryBarEffect.draw(leds, BatteryReading.parse(fields("1", "10000"), NOW), 0, 15);
        check(leds.pixel(0, 15).g() == 66 && leds.pixel(1, 15).g() == 0, "low SOC clips power with partial fill");
        check(!Double.isFinite(BatteryReading.parse(new String[0], NOW).soc), "old endpoint supported");
        check(!Double.isFinite(BatteryReading.parse(fields("101", "NaN"), NOW).soc), "invalid SOC rejected");
        String[] stale = fields("50", "500");
        stale[34] = String.valueOf(NOW - 43201);
        BatteryReading reading = BatteryReading.parse(stale, NOW);
        check(!Double.isFinite(reading.soc) && !Double.isFinite(reading.power), "stale power hides entire bar");
        stale[33] = String.valueOf(NOW - 86400);
        stale[34] = String.valueOf(NOW);
        check(BatteryReading.parse(stale, NOW).soc == 50, "old SOC remains valid with live power");
        stale[33] = "";
        check(BatteryReading.parse(stale, NOW).soc == 50, "SOC timestamp is optional");
        stale[31] = "0";
        stale[32] = "0";
        stale[34] = String.valueOf(NOW - 43200);
        check(BatteryReading.parse(stale, NOW).soc == 0, "empty idle battery valid for twelve hours");
        stale[32] = "";
        check(!Double.isFinite(BatteryReading.parse(stale, NOW).soc), "missing power hides bar");
        check(!Double.isFinite(BatteryReading.parse(fields("50", "0"), NOW - 31).soc), "future timestamp rejected");
        leds.reset();
        BatteryBarEffect.draw(leds, BatteryReading.parse(fields("100", "0"), NOW), 0, 15);
        check(leds.pixel(59, 15).b() == 65, "full battery");
        BatteryBarEffect.draw(leds, BatteryReading.parse(fields("0", "-2400"), NOW), 0, 15);
        check(leds.pixel(0, 15).r() == 0, "empty battery");
    }

    private static void network() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        AtomicInteger mode = new AtomicInteger();
        AtomicInteger pvCalls = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            try {
                String path = exchange.getRequestURI().getPath();
                if (path.equals("/bad")) {
                    exchange.sendResponseHeaders(503, -1);
                    return;
                }
                byte[] body;
                if (path.startsWith("/pv/") || path.startsWith("/d0/")) {
                    pvCalls.incrementAndGet();
                    body = ("var wr_pac=[1200,0];\nvar d0_pac=[-200];\n"
                        + "var d0_ev_pac_vals=[500,600];\nvar d0_pac_vals=[-100,-200];\n").getBytes("UTF-8");
                } else {
                    calls.incrementAndGet();
                    Thread.sleep(mode.get() == 0 ? 300 : 3000);
                    body = String.join(";", fields("60", "2000")).getBytes("UTF-8");
                }
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            } catch (Exception ignored) {
            } finally { exchange.close(); }
        });
        server.start();
        try {
            String base = "http://127.0.0.1:" + server.getAddress().getPort();
            Files.write(Paths.get("global.properties"), Arrays.asList(
                "net.netio_1og.url = " + base + "/",
                "net.pvdata.url = " + base + "/pv/", "net.d0data.url = " + base + "/d0/"));
            NetIO1OG client = NetIO1OG.getInstance();
            PvData pv = PvData.getInstance();
            long start = System.nanoTime();
            for (int i = 0; i < 1000; i++) client.getBatteryReading();
            for (int i = 0; i < 1000; i++) {
                pv.getCurrentPac();
                pv.getCurrentD0Pac();
                pv.getCurrentSelfConsumption();
            }
            check(System.nanoTime() - start < 250000000L, "initial request must not block rendering");
            Thread.sleep(800);
            check(calls.get() == 1, "only one request in flight");
            check("21.5".equals(client.getTempAussenKueche()), "other NETIO fields refreshed");
            check(client.getStateLichtBad(), "boolean fields parsed by value");
            check(pv.getCurrentPac() == 1200 && pv.getCurrentD0Pac() == -200, "PV and D0 refresh asynchronously");
            check(pv.getCurrentSelfConsumption() == 600, "PV history refreshed");
            check(pvCalls.get() == 3, "PV, D0 and yesterday fetched once without duplicate workers");
            check(FileHelper.getFileAsList(base + "/bad").isEmpty(), "HTTP errors skipped safely");
            java.lang.reflect.Field next = NetIO1OG.class.getDeclaredField("_nextAttempt");
            next.setAccessible(true);
            next.setLong(client, 0);
            mode.set(1);
            client.getBatteryReading();
            Thread.sleep(2500);
            for (int i = 0; i < 1000; i++) client.getBatteryReading();
            check(calls.get() == 2, "timeout backs off instead of queuing requests");
            check("21.5".equals(client.getTempAussenKueche()), "timeout preserves previous values");
        } finally { server.stop(0); }
    }

    public static void main(String[] args) throws Exception {
        render();
        network();
        System.out.println("Battery rendering, freshness and network checks passed.");
    }
}
