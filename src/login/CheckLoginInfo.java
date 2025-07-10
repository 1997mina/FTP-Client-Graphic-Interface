package login;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import ui.FileList;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.ConnectException;
import java.net.Socket;

public class CheckLoginInfo implements ActionListener {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JFrame loginFrame;
    private final JLabel errorLabel;
    private static int loginAttempts = 0;
    private static final int MAX_LOGIN_ATTEMPTS = 3;

    public CheckLoginInfo(JTextField usernameField, JPasswordField passwordField, JFrame loginFrame, JLabel errorLabel) {
        this.usernameField = usernameField;
        this.passwordField = passwordField;
        this.loginFrame = loginFrame;
        this.errorLabel = errorLabel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        // Xóa thông báo lỗi cũ mỗi khi nhấn nút
        errorLabel.setText(" ");
        
        Socket socket = null;
        BufferedReader reader = null;
        PrintWriter writer = null;

        try {
            socket = new Socket("127.0.0.1", 21);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            // Đọc thông điệp chào mừng (mã 220)
            String response = reader.readLine();
            if (response == null || !response.startsWith("220")) {
                JOptionPane.showMessageDialog(null, "FTP server chưa sẵn sàng: " + response, "Lỗi Kết Nối", JOptionPane.ERROR_MESSAGE);
                closeResources(socket, reader, writer); // Đóng khi có lỗi
                return;
            }

            // Send USER command
            writer.println("USER " + username);
            response = reader.readLine();
            if (response == null || !response.startsWith("331")) { // 331: Yêu cầu mật khẩu
                errorLabel.setText("Thông tin đăng nhập không chính xác");
                handleFailedLogin();
                closeResources(socket, reader, writer); // Đóng khi có lỗi
                return;
            }

            // Send PASS command
            writer.println("PASS " + password);
            String passResponse = reader.readLine();

            // Kiểm tra đăng nhập thành công (mã 230)
            if (passResponse != null && passResponse.startsWith("230")) {
                loginAttempts = 0; // Reset attempts on successful login
                // Đăng nhập thành công, truyền socket và các luồng đang mở đi
                showFileList(socket, reader, writer);
            } else {
                errorLabel.setText("Thông tin đăng nhập không chính xác");
                handleFailedLogin();
                closeResources(socket, reader, writer); // Đóng khi đăng nhập sai
            }
        } catch (ConnectException ex) {
            JOptionPane.showMessageDialog(null, "Không thể kết nối đến FTP server. Hãy đảm bảo server đang chạy.", "Lỗi Kết Nối", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Lỗi giao tiếp với FTP server: " + ex.getMessage(), "Lỗi Kết Nối", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            closeResources(socket, reader, writer); // Đóng khi có lỗi IO khác
        }
    }

    private void handleFailedLogin() {
        loginAttempts++;
        if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
            JOptionPane.showMessageDialog(null, "Quá nhiều lần đăng nhập thất bại. Ứng dụng sẽ thoát.", "Cảnh Báo Bảo Mật", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private void showFileList(Socket socket, BufferedReader controlReader, PrintWriter controlWriter) {
        // Đóng cửa sổ đăng nhập
        loginFrame.dispose();

        // Hiển thị cửa sổ danh sách tệp mới, truyền trực tiếp các luồng điều khiển.
        // Giao diện mới sẽ tự xử lý việc lấy danh sách tệp.
        SwingUtilities.invokeLater(() -> new FileList(socket, controlReader, controlWriter));
    }

    // Phương thức trợ giúp để đóng tài nguyên, tránh lặp code
    private void closeResources(Socket s, BufferedReader r, PrintWriter w) {
        try {
            if (w != null) w.close();
            if (r != null) r.close();
            if (s != null && !s.isClosed()) s.close();
        } catch (IOException e) {
            // Ghi lại lỗi nếu cần, nhưng thường có thể bỏ qua khi đang dọn dẹp
            e.printStackTrace();
        }
    }
}
