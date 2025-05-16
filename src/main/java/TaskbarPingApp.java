import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.util.concurrent.*;

public class TaskbarPingApp {
    private final int greenThreshold, yellowThreshold;
    private final Color greenBg, yellowBg, redBg;
    private final Color greenText, yellowText, redText;
    private final JFrame frame;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public TaskbarPingApp(int g, int y,
                          Color gc, Color yc, Color rc,
                          Color gtc, Color ytc, Color rtc) {
        greenThreshold = g;
        yellowThreshold = y;
        greenBg = gc; yellowBg = yc; redBg = rc;
        greenText = gtc; yellowText = ytc; redText = rtc;

        // minimal frame for taskbar
        frame = new JFrame();
        frame.setUndecorated(true);
        frame.setSize(1,1);
        frame.setLocation(-100,-100);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        scheduler.scheduleAtFixedRate(this::updateIcon, 0, 2, TimeUnit.SECONDS);
    }

    public static void run(int g, int y,
                           Color gc, Color yc, Color rc,
                           Color gtc, Color ytc, Color rtc) {
        SwingUtilities.invokeLater(() -> new TaskbarPingApp(
                g, y,
                gc, yc, rc,
                gtc, ytc, rtc));
    }

    private void updateIcon() {
        int latency = ping("8.8.8.8");
        BufferedImage img = drawIcon(latency);
        frame.setIconImage(img);
    }

    private int ping(String host) {
        try {
            long start = System.nanoTime();
            if (InetAddress.getByName(host).isReachable(1000)) {
                return (int)((System.nanoTime() - start) / 1_000_000);
            }
        } catch (Exception ignored) {}
        return -1;
    }

    private BufferedImage drawIcon(int latency) {
        int size = 32, arc = 8;
        Color bg, text;
        String txt = (latency < 0 ? "ERR" : String.valueOf(latency));
        if (latency < 0 || latency > yellowThreshold) {
            bg = redBg; text = redText;
        } else if (latency > greenThreshold) {
            bg = yellowBg; text = yellowText;
        } else {
            bg = greenBg; text = greenText;
        }

        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, size, size, arc, arc);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(txt);
        int h = fm.getAscent();
        g2.setColor(text);
        g2.drawString(txt, (size - w) / 2, (size + h) / 2 - 2);
        g2.dispose();
        return img;
    }
}