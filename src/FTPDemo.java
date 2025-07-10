import javax.swing.SwingUtilities;

import ui.Login;

public class FTPDemo {
    public static void main(String[] args) {
        // Run the GUI creation on the Event Dispatch Thread (EDT) for thread safety
        SwingUtilities.invokeLater(() -> new Login());
    }
}
