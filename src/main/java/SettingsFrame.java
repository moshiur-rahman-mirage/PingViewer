import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;

public class SettingsFrame extends JFrame {
    // creating two spinner, orientation doesn't mean anything. it is kind of creating variable. which will later be added in canvus.
    private final JSpinner greenThreshold = new JSpinner(new SpinnerNumberModel(40, 1, 10000, 10));
    private final JSpinner yellowThreshold = new JSpinner(new SpinnerNumberModel(500, 1, 10000, 10));
    private final JTextField hostname = new JTextField();
    // creating buttons to pick color.
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


    // painting the canvus start from below.
    public SettingsFrame() {
        super("Ping Taskbar App Setup");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(320, 320);
        hostname.setText("8.8.8.8");
        // this is the actual canvus.
        // Creates a new JPanel named content to hold other Swing components (e.g., buttons, labels, spinners).
        // The number of rows in the grid. 0 means "auto-determine" based on the number of components and columns.
        JPanel content = new JPanel(new GridLayout(0, 2, 5, 5));
        // Sets a border around the JPanel (content) to create spacing between the panel's edges and its internal components.
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


        content.add(new JLabel("Host:"));
        content.add(hostname);
        content.add(autoStartCb);
        content.add(new JLabel());
        content.add(runBtn);
        content.add(new JLabel());

        setContentPane(content);


    // setting color from clicking green button using JColorChooser
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

//        runBtn.addActionListener(this::onRun);

        runBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onRun(e);
            }
        });

        // Centers the window on the screen when argument is null
        //If i pass a component instead of null, it centers relative to that component
        setLocationRelativeTo(null);

    }

    private void onRun(ActionEvent e) {
        int g = (int) greenThreshold.getValue();
        int y = (int) yellowThreshold.getValue();
        boolean auto = autoStartCb.isSelected();
        String host = hostname.getText();
        // launch with 3 bg and 3 text colors
        TaskbarPingApp.run(g, y,
                greenBg, yellowBg, redBg,
                greenText, yellowText, redText,host);

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
        // 1. Get startup folder
        String startup = System.getenv("APPDATA")
                + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";

        // 2. Get path of the JAR (shaded JAR, not classes dir)
        File jar = new File(Main.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());

        // 3. If running from 'target/classes', adjust to point to the shaded JAR
        if (jar.isDirectory()) {
            // Assume Maven structure: go up from 'target/classes' to 'target'
            File targetDir = jar.getParentFile().getParentFile();
            String jarName = "ping-taskbar.jar"; // update if needed
            jar = new File(targetDir, "target/" + jarName);
        }

        // 4. Create the .bat content
        String batName = startup + "\\PingTaskbarAppStart.bat";
        String content = "@echo off\r\n"
                + "start \"\" javaw -jar \"" + jar.getAbsolutePath() + "\"\r\n";

        // 5. Write the .bat file
        try (FileWriter fw = new FileWriter(batName)) {
            fw.write(content);
        }
    }

}