package MainWindow;

import methods.*;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import MainWindow.filemanager.FTPFile;

import java.awt.Cursor;
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
    public final JButton searchButton;
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
        searchButton = createToolbarButton("search01.png", "Tìm kiếm");
        createDirButton = createToolbarButton("newfolder.png", "Thư mục mới");

        // Thêm các nút vào thanh công cụ
        add(backButton);
        add(refreshButton);
        add(searchButton);
        addSeparator();
        add(createDirButton);
        add(renameButton);
        add(deleteButton);
        addSeparator();
        add(downloadButton);
        add(uploadButton);

        // Gán các trình xử lý sự kiện cho các thành phần UI, chẳng hạn như các nút trên thanh công cụ.
        backButton.addActionListener(e -> Cdup.handleBackAction(fileList));
        refreshButton.addActionListener(e -> fileList.refreshFileList());
        deleteButton.addActionListener(e -> Delete.handleDeleteAction(fileList));
        renameButton.addActionListener(e -> Rename.handleRenameAction(fileList));
        uploadButton.addActionListener(e -> Store.handleUploadAction(fileList));
        createDirButton.addActionListener(e -> Mkdir.initiateCreateDirectory(fileList));
        downloadButton.addActionListener(e -> Retrieve.handleDownloadAction(fileList));
        searchButton.addActionListener(e -> fileList.toggleSearchBar());
    }

    /**
     * Cập nhật trạng thái (enabled/disabled) của các nút trên thanh công cụ
     * dựa trên mục đang được chọn trong bảng.
     */
    public void updateButtonStates() {
        // Nếu đang trong quá trình chỉnh sửa (đổi tên hoặc tạo mới), vô hiệu hóa tất cả các nút
        if (fileList.editMode != FileList.EditMode.NONE) {
            backButton.setEnabled(false);
            refreshButton.setEnabled(false);
            downloadButton.setEnabled(false);
            uploadButton.setEnabled(false);
            deleteButton.setEnabled(false);
            renameButton.setEnabled(false);
            createDirButton.setEnabled(false);
            return;
        }

        int[] selectedRows = fileList.getFileTable().getSelectedRows();
        int selectionCount = selectedRows.length;

        // Vô hiệu hóa nút "Quay lại" nếu đang ở thư mục gốc
        String currentPath = fileList.getCurrentPath();
        backButton.setEnabled(currentPath != null && !currentPath.equals("/"));

        uploadButton.setEnabled(true);
        backButton.setEnabled(true);
        refreshButton.setEnabled(true);
        createDirButton.setEnabled(true);

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
    @SuppressWarnings("unused")
    private JButton createToolbarButton(String iconFileName, String buttonText) {
        String iconPath = "img/" + iconFileName;
        File iconFile = new File(iconPath);

        JButton button = new JButton();

        if (iconFile.exists()) {
            ImageIcon originalIcon = new ImageIcon(iconPath);
            Image scaledImage = originalIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaledImage));
        } else {
            System.err.println("Không tìm thấy icon tại đường dẫn: " + iconFile.getAbsolutePath());
        }

        button.setToolTipText(buttonText);
        button.setFocusable(false);
        // Đặt con trỏ thành hình bàn tay chỉ khi nút đó không bị vô hiệu hóa
        button.addChangeListener(e -> {
            if (button.isEnabled()) {
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else {
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        return button;
    }
}
