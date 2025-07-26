package MainWindow;

import MainWindow.filemanager.FTPFile;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

/**
 * Lớp này hiển thị một hộp thoại (dialog) chứa các thuộc tính chi tiết
 * của một tệp hoặc thư mục được chọn.
 */
public class Properties extends JDialog {

    /**
     * Tạo và hiển thị hộp thoại thuộc tính.
     * @param owner Cửa sổ cha (thường là FileList) mà hộp thoại này thuộc về.
     * @param file Đối tượng FTPFile chứa thông tin của tệp/thư mục.
     * @param path Đường dẫn hiện tại nơi tệp/thư mục đang tọa lạc.
     */
    @SuppressWarnings("unused")
    public Properties(Frame owner, FTPFile file, String path) {
        super(owner, "Thuộc tính: " + file.getName(), true); // true để làm cho dialog modal
        setSize(450, 280);
        setResizable(false);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // Panel chính với GridBagLayout để sắp xếp các thành phần
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // --- Hàng 0: Tên ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0; // Cột label không co giãn
        panel.add(new JLabel("Tên:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1; // Cột giá trị co giãn để lấp đầy không gian
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(file.getName()), gbc);

        // --- Hàng 1: Loại ---
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Loại:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        String type = file.isDirectory() ? "Thư mục" : "Tệp tin";
        panel.add(new JLabel(type), gbc);

        // --- Hàng 2: Vị trí ---
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Vị trí:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(path), gbc);

        // --- Hàng 3: Kích thước ---
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Kích thước:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        // Hiển thị kích thước chi tiết cho tệp, và "N/A" cho thư mục
        String size = file.isDirectory() ? "N/A" : file.getFormattedSize() + " (" + String.format("%,d", file.getSize()) + " bytes)";
        panel.add(new JLabel(size), gbc);

        // --- Hàng 4: Ngày sửa đổi ---
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Ngày sửa đổi:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss, dd/MM/yyyy");
        panel.add(new JLabel(sdf.format(file.getLastModified())), gbc);

        // --- Nút OK ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> dispose()); // Đóng dialog khi nhấn nút
        buttonPanel.add(okButton);

        // Thêm các panel vào dialog
        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Hiển thị dialog
        setVisible(true);
    }
}
