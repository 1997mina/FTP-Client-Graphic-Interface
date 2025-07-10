package ui;

import filemanager.FTPFile;
import methods.Delete;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Lớp đại diện cho thanh công cụ chính của ứng dụng.
 * Chứa các nút chức năng và logic tạo ra chúng.
 */
public class Toolbar extends JToolBar {

    private final FileList fileList;

    // Các nút được để public để lớp FileList có thể thêm ActionListener
    public final JButton backButton;
    public final JButton refreshButton;
    public final JButton downloadButton;
    public final JButton uploadButton;
    public final JButton deleteButton;

    public Toolbar(FileList fileList) {
        this.fileList = fileList;

        setFloatable(false); // Không cho phép di chuyển thanh công cụ

        // Khởi tạo các nút
        backButton = createToolbarButton("back.png", "Quay lại (chưa hoạt động)");
        refreshButton = createToolbarButton("refresh.png", "Tải lại");
        downloadButton = createToolbarButton("download.png", "Tải xuống (chưa hoạt động)");
        uploadButton = createToolbarButton("upload.png", "Tải lên (chưa hoạt động)");
        deleteButton = createToolbarButton("delete.png", "Xóa tệp");

        // Thêm các nút vào thanh công cụ
        add(backButton);
        add(refreshButton);
        addSeparator();
        add(downloadButton);
        add(uploadButton);
        add(deleteButton);

        addListeners();
    }

    /**
     * Gán các trình xử lý sự kiện cho các thành phần UI, chẳng hạn như các nút trên thanh công cụ.
     */
    @SuppressWarnings("unused")
    private void addListeners() {
        refreshButton.addActionListener(e -> fileList.refreshFileList());
        deleteButton.addActionListener(e -> handleDeleteAction());
        fileList.getFileTable().getSelectionModel().addListSelectionListener(e -> updateButtonStates());
    }

    /**
     * Xử lý hành động xóa tệp khi người dùng nhấn nút Xóa.
     */
    private void handleDeleteAction() {
        int selectedRow = fileList.getFileTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(fileList, "Vui lòng chọn một tệp để xóa.", "Chưa chọn tệp", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        FTPFile fileToDelete = fileList.getCurrentFiles().get(selectedRow);

        if (fileToDelete.isDirectory()) {
            JOptionPane.showMessageDialog(fileList, "Không thể xóa thư mục bằng chức năng này.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(fileList, "Bạn có chắc chắn muốn xóa tệp '" + fileToDelete.getName() + "' không?\nHành động này không thể hoàn tác.", "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                PrintWriter writer = fileList.getControlWriter();
                BufferedReader reader = fileList.getControlReader();
                if (Delete.deleteFile(writer, reader, fileToDelete.getName())) {
                    JOptionPane.showMessageDialog(fileList, "Đã xóa tệp thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    fileList.refreshFileList(); // Tải lại danh sách tệp
                } else {
                    JOptionPane.showMessageDialog(fileList, "Không thể xóa tệp. Vui lòng kiểm tra quyền hạn của bạn.", "Lỗi Xóa", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(fileList, "Lỗi mạng khi đang xóa tệp: " + ex.getMessage(), "Lỗi Mạng", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Cập nhật trạng thái (enabled/disabled) của các nút trên thanh công cụ
     * dựa trên mục đang được chọn trong bảng.
     */
    public void updateButtonStates() {
        int selectedRow = fileList.getFileTable().getSelectedRow();

        // Vô hiệu hóa các nút nếu không có mục nào được chọn
        if (selectedRow == -1) {
            downloadButton.setEnabled(false);
            uploadButton.setEnabled(false);
            deleteButton.setEnabled(false);
            return;
        }

        // Kích hoạt các nút dựa trên loại mục được chọn (tệp hay thư mục)
        FTPFile selectedFile = fileList.getCurrentFiles().get(selectedRow);
        boolean isFile = !selectedFile.isDirectory();
        downloadButton.setEnabled(isFile);
        uploadButton.setEnabled(true); // Luôn cho phép tải lên khi có chọn
        deleteButton.setEnabled(isFile);
    }

    /**
     * Phương thức trợ giúp để tạo một JButton với icon và tooltip.
     * Icon được tải từ thư mục 'img' nằm cùng cấp với thư mục 'src'.
     * @param iconFileName Tên file icon (ví dụ: "back.png").
     * @param toolTipText  Văn bản chú thích khi di chuột qua.
     * @return một đối tượng JButton.
     */
    private JButton createToolbarButton(String iconFileName, String toolTipText) {
        String iconPath = "img/" + iconFileName;
        File iconFile = new File(iconPath);

        // Lấy phần văn bản chính cho nút từ tooltip một cách an toàn
        String buttonText = toolTipText;
        int parenthesisIndex = toolTipText.indexOf('(');
        if (parenthesisIndex != -1) {
            // Nếu có dấu ngoặc đơn, chỉ lấy phần văn bản phía trước
            buttonText = toolTipText.substring(0, parenthesisIndex).trim();
        }
        JButton button = new JButton(buttonText);

        if (iconFile.exists()) {
            ImageIcon originalIcon = new ImageIcon(iconPath);
            Image scaledImage = originalIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaledImage));
        } else {
            System.err.println("Không tìm thấy icon tại đường dẫn: " + iconFile.getAbsolutePath());
        }

        button.setToolTipText(toolTipText);
        button.setFocusable(false);
        return button;
    }
}
