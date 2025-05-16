import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;

public class SettingsFrame extends JFrame {
    private final JSpinner greenThreshold = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 50));
    private final JSpinner yellowThreshold = new JSpinner(new SpinnerNumberModel(500, 1, 10000, 50));

    private final JButton greenBgBtn = new JButton("Pick");
    private final JButton yellowBgBtn = new JButton("Pick");
    private final JButton redBgBtn = new JButton("Pick");

    private final JButton greenTextBtn = new JButton("Pick");
    private final JButton yellowTextBtn = new JButton("Pick");
    private final JButton redTextBtn = new JButton("Pick");

    private Color greenBg = Color.decode("#4CAF50"),
            yellowBg = Color.decode("#FFEB3B"),
            redBg = Color.decode("#F44336");
    private Color greenText = Color.decode("#FFFFFF"),
            yellowText = Color.decode("#000000"),
            redText = Color.decode("#FFFFFF");

    private final JCheckBox autoStartCb = new JCheckBox("Auto-start on Windows login", true);
    private final JButton runBtn = new JButton("Run");

    public SettingsFrame() {
        super("Ping Taskbar App Setup");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(320, 320);

        JPanel content = new JPanel(new GridLayout(0, 2, 5, 5));
        content.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(content);

        content.add(new JLabel("Green ≤ (ms):"));
        content.add(greenThreshold);
        content.add(new JLabel("Green BG color:"));
        content.add(greenBgBtn);
        content.add(new JLabel("Green text color:"));
        content.add(greenTextBtn);

        content.add(new JLabel("Yellow ≤ (ms):"));
        content.add(yellowThreshold);
        content.add(new JLabel("Yellow BG color:"));
        content.add(yellowBgBtn);
        content.add(new JLabel("Yellow text color:"));
        content.add(yellowTextBtn);

        content.add(new JLabel("Red BG color:"));
        content.add(redBgBtn);
        content.add(new JLabel("Red text color:"));
        content.add(redTextBtn);

        content.add(autoStartCb);
        content.add(new JLabel());
        content.add(runBtn);
        content.add(new JLabel());


        add(autoStartCb);
        add(new JLabel());
        add(runBtn);
        add(new JLabel());

        greenBgBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Choose green background color", greenBg);
            if (c != null) greenBg = c;
        });
        yellowBgBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Choose yellow background color", yellowBg);
            if (c != null) yellowBg = c;
        });
        redBgBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Choose red background color", redBg);
            if (c != null) redBg = c;
        });

        greenTextBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Choose green text color", greenText);
            if (c != null) greenText = c;
        });
        yellowTextBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Choose yellow text color", yellowText);
            if (c != null) yellowText = c;
        });
        redTextBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Choose red text color", redText);
            if (c != null) redText = c;
        });

        runBtn.addActionListener(this::onRun);
        setLocationRelativeTo(null);
    }

    private void onRun(ActionEvent e) {
        int g = (int) greenThreshold.getValue();
        int y = (int) yellowThreshold.getValue();
        boolean auto = autoStartCb.isSelected();

        // launch with 3 bg and 3 text colors
        TaskbarPingApp.run(g, y,
                greenBg, yellowBg, redBg,
                greenText, yellowText, redText);

        if (auto) {
            try {
                createStartupBat();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        dispose();
    }

    private void createStartupBat() throws Exception {
        String startup = System.getenv("APPDATA")
                + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
        File jar = new File(Main.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
        String batName = startup + "\\PingTaskbarAppStart.bat";
        String content = "@echo off\r\n"
                + "java -jar \"" + jar.getAbsolutePath() + "\"\r\n";
        try (FileWriter fw = new FileWriter(batName)) {
            fw.write(content);
        }
    }
}