import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SettingsFrame frame = new SettingsFrame();
            frame.setVisible(true);
        });
    }
}