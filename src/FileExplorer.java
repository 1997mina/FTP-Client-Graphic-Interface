import javax.swing.SwingUtilities;

import LoginWindow.Login;

public class FileExplorer {
    public static void main(String[] args) {
        // Run the GUI creation on the Event Dispatch Thread (EDT) for thread safety
        SwingUtilities.invokeLater(() -> new Login());
    }
}
