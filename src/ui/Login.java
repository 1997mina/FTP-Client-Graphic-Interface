package ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import login.CheckLoginInfo;

public class Login extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel errorLabel;

    public Login() {
        setTitle("Đăng nhập");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        // Create components
        JLabel usernameLabel = new JLabel("Tài khoản:");
        usernameField = new JTextField(15);

        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordField = new JPasswordField(15);

        loginButton = new JButton("Đăng nhập");
        loginButton.setBackground(Color.BLUE); // Blue color

        errorLabel = new JLabel(" "); // Dùng một khoảng trắng để giữ chỗ
        errorLabel.setForeground(Color.RED);

        // Set up layout (using GridBagLayout for better control)
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding

        // Add username label and field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(usernameField, gbc);

        // Add password label and field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(passwordField, gbc);

        // Add login button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // Span two columns
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(loginButton, gbc);

        // Thêm nhãn báo lỗi
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(errorLabel, gbc);

        // Add action listener to the login button
        loginButton.addActionListener(new CheckLoginInfo(usernameField, passwordField, this, errorLabel));

        // Add the panel to the frame and make it visible
        add(panel);
        setVisible(true);
    }
}