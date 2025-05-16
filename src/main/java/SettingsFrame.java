import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;

public class SettingsFrame extends JFrame {
    private final JSpinner greenThreshold = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 50));
    private final JSpinner yellowThreshold = new JSpinner(new SpinnerNumberModel(500, 1, 10000, 50));
    private final JButton greenColorBtn = new JButton("Pick");
    private final JButton yellowColorBtn = new JButton("Pick");
    private final JButton redColorBtn = new JButton("Pick");
    private Color greenColor = Color.GREEN, yellowColor = Color.YELLOW, redColor = Color.RED;
    private final JCheckBox autoStartCb = new JCheckBox("Auto-start on Windows login", true);
    private final JButton runBtn = new JButton("Run");

    public SettingsFrame() {
        super("Ping Tray App Setup");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(350, 300);
        setLayout(new GridLayout(0,2,5,5));
        add(new JLabel("Green ≤ (ms):"));   add(greenThreshold);
        add(new JLabel("Yellow ≤ (ms):"));  add(yellowThreshold);
        add(new JLabel("Green color:"));    add(greenColorBtn);
        add(new JLabel("Yellow color:"));   add(yellowColorBtn);
        add(new JLabel("Red color:"));      add(redColorBtn);
        add(autoStartCb);                   add(new JLabel());
        add(runBtn);                        add(new JLabel());

        greenColorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Choose green", greenColor);
            if(c!=null) greenColor = c;
        });
        yellowColorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Choose yellow", yellowColor);
            if(c!=null) yellowColor = c;
        });
        redColorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Choose red", redColor);
            if(c!=null) redColor = c;
        });

        runBtn.addActionListener(this::onRun);
        setLocationRelativeTo(null);
    }

    private void onRun(ActionEvent e) {
        int g = (int)greenThreshold.getValue();
        int y = (int)yellowThreshold.getValue();
        boolean auto = autoStartCb.isSelected();
        // Launch display window
        SwingUtilities.invokeLater(() -> {
            PingDisplayWindow win = new PingDisplayWindow(g, y, greenColor, yellowColor, redColor);
            win.setVisible(true);
        });
        // Auto-start
        if(auto) {
            try { createStartupBat(); }
            catch(Exception ex){ ex.printStackTrace(); }
        }
        dispose();
    }

    private void createStartupBat() throws Exception {
        String startup = System.getenv("APPDATA")
                + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
        // Get path to our running JAR
        File jar = new File(Main.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI());
        String batName = startup + "\\PingTrayAppStart.bat";
        String content = "@echo off\r\n"
                + "java -jar \"" + jar.getAbsolutePath() + "\"\r\n";
        try (FileWriter fw = new FileWriter(batName)) {
            fw.write(content);
        }
    }
}
