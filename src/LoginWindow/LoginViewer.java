package LoginWindow;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.ConnectException;

import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class LoginViewer extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel errorLabel;
    private JCheckBox showPasswordCheckBox;

    @SuppressWarnings("unused")
    public LoginViewer() {
        // Kiểm tra trạng thái server ngay khi khởi động
        checkServerStatus();

        setTitle("Đăng nhập");
        setSize(300, 250); // Đặt kích thước cửa sổ
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        // Create components
        JLabel usernameLabel = new JLabel("Tài khoản:");
        usernameField = new JTextField(15);

        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordField = new JPasswordField(15);

        showPasswordCheckBox = new JCheckBox("Hiện mật khẩu");

        loginButton = new JButton("Đăng nhập");
        loginButton.setBackground(Color.BLUE); // Đặt màu nền xanh cho nút

        errorLabel = new JLabel(" "); // Dùng một khoảng trắng để giữ chỗ
        errorLabel.setForeground(Color.RED);

        // Set up layout (using GridBagLayout for better control)
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding

        // Add username label and field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST; // Căn lề phải
        panel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST; // Căn lề trái
        panel.add(usernameField, gbc);

        // Add password label and field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST; // Căn lề phải
        panel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST; // Căn lề trái
        panel.add(passwordField, gbc);

        // Add show password checkbox
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(showPasswordCheckBox, gbc);

        // Add login button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2; // Nút chiếm 2 cột
        gbc.anchor = GridBagConstraints.CENTER; // Căn giữa
        panel.add(loginButton, gbc);

        // Thêm nhãn báo lỗi
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(errorLabel, gbc);

        // Add action listener to the login button
        loginButton.addActionListener(new CheckLoginInfo(usernameField, passwordField, this, errorLabel, loginButton)); // Thêm trình nghe sự kiện cho nút đăng nhập

        // Thêm trình nghe sự kiện cho checkbox hiện mật khẩu
        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setEchoChar((char) 0); // Hiện mật khẩu
            } else {
                passwordField.setEchoChar('•'); // Ẩn mật khẩu
            }
        });

        // Add the panel to the frame and make it visible
        add(panel);
        setVisible(true);
    }

    /**
     * Kiểm tra xem FTP server có đang chạy và sẵn sàng nhận kết nối hay không.
     * Phương thức này sẽ được gọi trước khi hiển thị cửa sổ đăng nhập.
     * Nếu không thể kết nối, một thông báo lỗi sẽ được hiển thị và ứng dụng sẽ thoát.
     */
    private void checkServerStatus() {
        // Cố gắng kết nối đến server trên cổng 21 với timeout 5 giây.
        try (Socket socket = new Socket()) {
            // Sử dụng connect với timeout để tránh treo ứng dụng nếu server không phản hồi
            socket.connect(new java.net.InetSocketAddress("127.0.0.1", 21), 5000);
            // Đọc phản hồi chào mừng (mã 220) để chắc chắn server sẵn sàng.
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = reader.readLine();
            if (response == null || !response.startsWith("220")) {
                JOptionPane.showMessageDialog(null, "FTP server không phản hồi đúng cách. Phản hồi: " + response, "Lỗi Server", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
            // Nếu kết nối và phản hồi thành công, server đang hoạt động.
            // try-with-resources sẽ tự động đóng socket.
        } catch (ConnectException e) {
            JOptionPane.showMessageDialog(null, "Không thể kết nối đến FTP server. Vui lòng đảm bảo server đang chạy trước khi khởi động ứng dụng.", "Lỗi Kết Nối Server", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        } catch (SocketTimeoutException e) {
            JOptionPane.showMessageDialog(null, "Kết nối đến server bị timeout. Server có thể đang bị treo hoặc mạng chậm.", "Lỗi Kết Nối Server", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Đã xảy ra lỗi I/O khi kiểm tra trạng thái server: " + e.getMessage(), "Lỗi I/O", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        // Run the GUI creation on the Event Dispatch Thread (EDT) for thread safety
        SwingUtilities.invokeLater(() -> new LoginViewer());
    }
}