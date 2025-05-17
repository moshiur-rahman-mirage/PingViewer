import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Map;
import java.util.concurrent.*;

public class TaskbarPingApp {
    private final int greenThreshold, yellowThreshold;
    private final Color greenBg, yellowBg, redBg;
    private final Color greenText, yellowText, redText;
    private final JFrame frame;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final String host;


    private static final int ICON_SIZE = 32;
    private static final int ARC = 8;
    private static final Font ICON_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Map<String, Integer> widthCache = new ConcurrentHashMap<>();
    private int maxAscent;


    {
        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tempImage.createGraphics();
        FontMetrics fm = g.getFontMetrics(ICON_FONT);
        maxAscent = fm.getAscent();
        g.dispose();
    }



    public static void printDetailedMemory() {
        MemoryMXBean memoryMxBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMxBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryMxBean.getNonHeapMemoryUsage();

        System.out.println("Heap: " + formatMemory(heapUsage));
        System.out.println("Non-Heap: " + formatMemory(nonHeapUsage));
    }

    private static String formatMemory(MemoryUsage usage) {
        return String.format(
                "init=%sMB, used=%sMB, committed=%sMB, max=%sMB",
                usage.getInit() / (1024 * 1024),
                usage.getUsed() / (1024 * 1024),
                usage.getCommitted() / (1024 * 1024),
                usage.getMax() / (1024 * 1024)
        );
    }


    public TaskbarPingApp(int g, int y,
                          Color gc, Color yc, Color rc,
                          Color gtc, Color ytc, Color rtc, String host) {
        greenThreshold = g;
        yellowThreshold = y;
        greenBg = gc; yellowBg = yc; redBg = rc;
        greenText = gtc; yellowText = ytc; redText = rtc;
        this.host = host;

        // minimal frame for taskbar
        frame = new JFrame();
        frame.setUndecorated(true);
        frame.setSize(1,1);
        frame.setLocation(100,100);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);


        scheduler.scheduleAtFixedRate(() -> {
            long before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            updateIcon();
            long after = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            if (after - before > 2 * 1024 * 1024) { // 2MB growth
                System.err.println("Memory spike detected: +" + (after - before) + " bytes");
            }
        }, 0, 2, TimeUnit.SECONDS);

    }

    public static void run(int g, int y,
                           Color gc, Color yc, Color rc,
                           Color gtc, Color ytc, Color rtc,String host) {
        SwingUtilities.invokeLater(() -> new TaskbarPingApp(
                g, y,
                gc, yc, rc,
                gtc, ytc, rtc,host));
    }

    private void updateIcon() {
        int latency = ping(host);
        BufferedImage img = drawIcon(latency);
        frame.setIconImage(img);
    }


    private int ping(String host) {
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder pb;

        if (os.contains("win")) {
            // Windows: ping -n 1 -w 1000 host
            pb = new ProcessBuilder("ping", "-n", "1", "-w", "1000", host);
        } else {
            // Unix/Linux/Mac: ping -c 1 -W 1 host
            pb = new ProcessBuilder("ping", "-c", "1", "-W", "1", host);
        }

        try {
            Process process = pb.start();
            boolean finished = process.waitFor(2, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                return -1;
            }

            String output = new String(process.getInputStream().readAllBytes());
            return parseLatency(output, os);
        } catch (Exception e) {
            System.err.println("Ping failed: " + e.getMessage());
            return -1;
        }
    }


    private int parseLatency(String output, String os) {
        try {
            if (os.contains("win")) {
                // Example line: "Average = 24ms"
                for (String line : output.split("\n")) {
                    if (line.contains("Average =")) {
                        String[] parts = line.split("Average =");
                        return Integer.parseInt(parts[1].replaceAll("[^\\d]", ""));
                    }
                }
            } else {
                // Example line: "time=24.8 ms"
                for (String line : output.split("\n")) {
                    if (line.contains("time=")) {
                        String[] parts = line.split("time=");
                        String timeStr = parts[1].split(" ")[0];
                        return (int) Math.round(Double.parseDouble(timeStr));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Latency parse error: " + e.getMessage());
        }
        return -1;
    }




    private BufferedImage drawIcon(int latency) {
        final String txt = (latency < 0 ? "ERR" : String.valueOf(latency));
        final Color bg, text;

        if (latency < 0 || latency > yellowThreshold) {
            bg = redBg; text = redText;
        } else if (latency > greenThreshold) {
            bg = yellowBg; text = yellowText;
        } else {
            bg = greenBg; text = greenText;
        }

        BufferedImage img = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw background
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, ICON_SIZE, ICON_SIZE, ARC, ARC);

            // Draw text with cached metrics
            g2.setFont(ICON_FONT);
            int width = widthCache.computeIfAbsent(txt, k ->
                    g2.getFontMetrics().stringWidth(k)
            );

            g2.setColor(text);
            g2.drawString(txt,
                    (ICON_SIZE - width) / 2,
                    (ICON_SIZE + maxAscent) / 2 - 2
            );
        } finally {
            g2.dispose();
        }
        return img;
    }


}