package MainWindow;

import methods.Delete;
import methods.Cdup;
import methods.Retrieve;
import methods.Rename;
import methods.Mkdir;
import methods.Store;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import MainWindow.filemanager.FTPFile;

import java.awt.Image;
import java.io.File;

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
    public final JButton renameButton;
    public final JButton createDirButton;

    @SuppressWarnings("unused")
    public Toolbar(FileList fileList) {
        this.fileList = fileList;

        setFloatable(false); // Không cho phép di chuyển thanh công cụ

        // Khởi tạo các nút
        backButton = createToolbarButton("back.png", "Quay lại");
        refreshButton = createToolbarButton("refresh.png", "Tải lại");
        renameButton = createToolbarButton("rename.png", "Đổi tên");
        downloadButton = createToolbarButton("download.png", "Tải xuống");
        uploadButton = createToolbarButton("upload.png", "Tải lên");
        deleteButton = createToolbarButton("delete.png", "Xóa");
        createDirButton = createToolbarButton("newfolder.png", "Thư mục mới");

        // Thêm các nút vào thanh công cụ
        add(backButton);
        add(refreshButton);
        addSeparator();
        add(createDirButton);
        add(renameButton);
        add(downloadButton);
        add(uploadButton);
        addSeparator();
        add(deleteButton);

        // Gán các trình xử lý sự kiện cho các thành phần UI, chẳng hạn như các nút trên thanh công cụ.
        backButton.addActionListener(e -> Cdup.handleBackAction(fileList));
        refreshButton.addActionListener(e -> fileList.refreshFileList());
        deleteButton.addActionListener(e -> Delete.handleDeleteAction(fileList));
        renameButton.addActionListener(e -> Rename.handleRenameAction(fileList));
        uploadButton.addActionListener(e -> Store.handleUploadAction(fileList));
        createDirButton.addActionListener(e -> Mkdir.handleCreateDirectoryAction(fileList));
        downloadButton.addActionListener(e -> Retrieve.handleDownloadAction(fileList));
    }

    /**
     * Cập nhật trạng thái (enabled/disabled) của các nút trên thanh công cụ
     * dựa trên mục đang được chọn trong bảng.
     */
    public void updateButtonStates() {
        int[] selectedRows = fileList.getFileTable().getSelectedRows();
        int selectionCount = selectedRows.length;

        // Vô hiệu hóa nút "Quay lại" nếu đang ở thư mục gốc
        String currentPath = fileList.getCurrentPath();
        backButton.setEnabled(currentPath != null && !currentPath.equals("/"));

        uploadButton.setEnabled(true);

        if (selectionCount == 0) { // Không có mục nào được chọn
            downloadButton.setEnabled(false);
            deleteButton.setEnabled(false);
            renameButton.setEnabled(false);
        } else if (selectionCount == 1) { // Có đúng một mục được chọn
            FTPFile selectedFile = fileList.getCurrentFiles().get(selectedRows[0]);
            boolean isFile = !selectedFile.isDirectory();

            downloadButton.setEnabled(isFile);
            deleteButton.setEnabled(true);
            renameButton.setEnabled(true);
        } else { // Có nhiều mục được chọn
            // Bật nút tải xuống nếu có ít nhất một tệp trong các mục đã chọn.
            boolean hasFile = false;
            for (int row : selectedRows) {
                if (!fileList.getCurrentFiles().get(row).isDirectory()) {
                    hasFile = true;
                    break;
                }
            }
            downloadButton.setEnabled(hasFile);
            deleteButton.setEnabled(true);    // Cho phép xóa nhiều mục
            renameButton.setEnabled(false);   // Không cho phép đổi tên nhiều mục
        }
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
