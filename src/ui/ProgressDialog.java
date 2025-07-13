package ui;

import javax.swing.*;
import java.awt.*;

/**
 * Một JDialog tùy chỉnh để hiển thị tiến trình truyền tệp.
 * Bao gồm một thanh tiến trình, nhãn cho tệp hiện tại và trạng thái tổng thể.
 */
public class ProgressDialog extends JDialog {
    private final JProgressBar progressBar;
    private final JLabel statusLabel;
    private final JLabel fileLabel;

    /**
     * Khởi tạo dialog tiến trình.
     *
     * @param owner   Cửa sổ cha (thường là FileList).
     * @param title   Tiêu đề của dialog (ví dụ: "Đang tải xuống...").
     */
    public ProgressDialog(Frame owner, String title) {
        super(owner, title, true); // true để làm cho nó modal
        setSize(400, 150);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // Ngăn người dùng đóng dialog
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        statusLabel = new JLabel("Đang chuẩn bị...");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        add(statusLabel, gbc);

        fileLabel = new JLabel(" ");
        fileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        add(fileLabel, gbc);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        add(progressBar, gbc);
    }

    /**
     * Cập nhật dialog để hiển thị tệp hiện tại đang được xử lý.
     *
     * @param filename         Tên của tệp.
     * @param currentFileNumber Số thứ tự của tệp hiện tại.
     * @param totalFiles       Tổng số tệp.
     */
    public void setCurrentFile(String filename, int currentFileNumber, int totalFiles) {
        // Chạy trên Event Dispatch Thread để đảm bảo an toàn cho luồng
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Đang xử lý tệp " + currentFileNumber + "/" + totalFiles);
            fileLabel.setText(filename);
            progressBar.setValue(0); // Reset thanh tiến trình cho tệp mới
        });
    }

    /**
     * Cập nhật giá trị của thanh tiến trình.
     *
     * @param percentage Giá trị tiến trình (0-100).
     */
    public void updateProgress(int percentage) {
        SwingUtilities.invokeLater(() -> progressBar.setValue(percentage));
    }

    /**
     * Đóng dialog.
     */
    public void closeDialog() {
        SwingUtilities.invokeLater(() -> {
            setVisible(false);
            dispose();
        });
    }
}