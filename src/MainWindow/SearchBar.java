package MainWindow;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
/**
 * Lớp này đại diện cho thanh tìm kiếm, bao gồm một trường nhập văn bản và các nút liên quan.
 * Nó đóng gói giao diện người dùng và các trình xử lý sự kiện cơ bản cho chức năng tìm kiếm.
 */
public class SearchBar extends JPanel {

    private final JTextField searchField;
    private final FileList fileList;

    /**
     * Tạo một thanh tìm kiếm mới.
     * @param fileList Tham chiếu đến cửa sổ FileList chính để tương tác.
     */
    @SuppressWarnings("unused")
    public SearchBar(FileList fileList) {
        this.fileList = fileList;
        // Sử dụng BorderLayout để có thể đẩy các nút sang phải
        setLayout(new BorderLayout(5, 2));
        setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));

        // Panel trung tâm chứa icon và trường tìm kiếm
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        centerPanel.setOpaque(false);

        // Tạo và thêm biểu tượng tìm kiếm
        JLabel searchIconLabel = new JLabel();
        String iconPath = "img/search02.png";
        File iconFile = new File(iconPath);

        if (iconFile.exists()) {
            ImageIcon originalIcon = new ImageIcon(iconPath);
            Image scaledImage = originalIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            searchIconLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            searchIconLabel.setText("Tìm kiếm:"); // Dự phòng nếu không tìm thấy icon
            System.err.println("Không tìm thấy icon tìm kiếm tại: " + iconFile.getAbsolutePath());
        }
        centerPanel.add(searchIconLabel);

        searchField = new JTextField(30);
        centerPanel.add(searchField);

        // Panel bên phải chứa các nút "Xóa" và "X"
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 1));
        rightPanel.setOpaque(false);

        JButton clearSearchButton = new JButton("Xóa");
        rightPanel.add(clearSearchButton);

        // Nút đóng 'X'
        JButton closeButton = new JButton("X");
        closeButton.setMargin(new Insets(0, 1, 0, 1));
        closeButton.setFont(new Font("Arial", Font.BOLD, 12));
        closeButton.setForeground(Color.GRAY);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.setFocusable(false);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        rightPanel.add(closeButton);

        // Thêm các panel vào SearchBar
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // Xóa ActionListener vì chúng ta sẽ sử dụng DocumentListener để tìm kiếm tức thì.
        // searchField.addActionListener(e -> this.fileList.searchFiles(searchField.getText()));

        // Thêm DocumentListener để tự động tìm kiếm mỗi khi văn bản thay đổi.
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fileList.searchFiles(searchField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fileList.searchFiles(searchField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Không cần thiết cho trường văn bản thuần túy.
            }
        });

        clearSearchButton.addActionListener(e -> {
            clearSearchText();
            this.fileList.searchFiles(""); // Gọi lại để reset tìm kiếm
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
