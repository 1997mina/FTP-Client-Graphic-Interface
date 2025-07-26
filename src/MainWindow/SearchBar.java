package MainWindow;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Lớp này đại diện cho thanh tìm kiếm, bao gồm một trường nhập văn bản và các nút liên quan.
 * Nó đóng gói giao diện người dùng và các trình xử lý sự kiện cơ bản cho chức năng tìm kiếm.
 */
public class SearchBar extends JPanel {

    private final JTextField searchField;

    /**
     * Tạo một thanh tìm kiếm mới.
     * @param fileList Tham chiếu đến cửa sổ FileList chính để tương tác.
     */
    @SuppressWarnings("unused")
    public SearchBar(FileList fileList) { // Thay đổi FlowLayout thành BorderLayout
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
        setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));

        // Tạo và thêm biểu tượng tìm kiếm
        JLabel searchIconLabel = new JLabel();
        String iconPath = "img/search02.png"; // Giả định có file search.png trong thư mục img
        File iconFile = new File(iconPath);

        if (iconFile.exists()) {
            ImageIcon originalIcon = new ImageIcon(iconPath);
            Image scaledImage = originalIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            searchIconLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            searchIconLabel.setText("Tìm kiếm:"); // Dự phòng nếu không tìm thấy icon
            System.err.println("Không tìm thấy icon tìm kiếm tại: " + iconFile.getAbsolutePath());
        }
        add(searchIconLabel);

        searchField = new JTextField(30);
        add(searchField);

        // Tạo một panel để chứa nút "Xóa" và nút "X"
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
        rightPanel.setOpaque(false); // Đảm bảo panel này trong suốt

        JButton clearSearchButton = new JButton("Xóa");
        add(clearSearchButton);

        // Tạo nút đóng 'X'
        JButton closeButton = new JButton("X");
        closeButton.setMargin(new Insets(0, 4, 0, 4));
        closeButton.setFont(new Font("Arial", Font.BOLD, 12));
        closeButton.setForeground(Color.GRAY);
        closeButton.setFocusable(false);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        rightPanel.add(closeButton);

        // Thêm rightPanel vào cuối SearchBar
        add(rightPanel);

        // Thêm trình nghe sự kiện (hiện đang được chú thích, sẵn sàng để triển khai)
        // searchField.addActionListener(e -> this.fileList.searchFiles(searchField.getText()));

        clearSearchButton.addActionListener(e -> {
            clearSearchText();
            // this.fileList.searchFiles(""); // Gọi lại để reset tìm kiếm
        });

        // Thêm hành động cho nút đóng để ẩn thanh tìm kiếm
        closeButton.addActionListener(e -> fileList.toggleSearchBar());
    }

    /**
     * Xóa văn bản trong trường tìm kiếm.
     */
    public void clearSearchText() {
        searchField.setText("");
    }

    /**
     * Đặt con trỏ (focus) vào trường nhập liệu tìm kiếm.
     */
    public void requestFocusInSearchField() {
        searchField.requestFocusInWindow();
    }
}
