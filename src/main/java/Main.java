import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SettingsFrame settings = new SettingsFrame();
            settings.setVisible(true);
        });
    }
}
