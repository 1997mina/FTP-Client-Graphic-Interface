package MainWindow.filemanager;

import java.util.List;
import java.util.ArrayList;

/**
 * Một lớp singleton để quản lý các hoạt động clipboard (sao chép, cắt, dán).
 */
public class ClipboardManager {

    public enum Operation {
        NONE,
        COPY, // Sao chép
        CUT   // Cắt
    }

    private static ClipboardManager instance;
    private List<FTPFile> files;
    private String sourcePath;
    private Operation operation;

    private ClipboardManager() {
        this.files = new ArrayList<>();
        this.operation = Operation.NONE;
    }

    /**
     * Lấy thực thể singleton của ClipboardManager.
     * @return Thực thể singleton.
     */
    public static synchronized ClipboardManager getInstance() {
        if (instance == null) {
            instance = new ClipboardManager();
        }
        return instance;
    }

    /**
     * Sao chép một danh sách các tệp vào clipboard.
     * @param filesToCopy Danh sách các đối tượng FTPFile để sao chép.
     * @param sourcePath Đường dẫn nơi các tệp đang được sao chép.
     */
    public void copy(List<FTPFile> filesToCopy, String sourcePath) {
        this.files.clear();
        this.files.addAll(filesToCopy);
        this.sourcePath = sourcePath;
        this.operation = Operation.COPY;
    }

    /**
     * Cắt một danh sách các tệp vào clipboard.
     * @param filesToCut Danh sách các đối tượng FTPFile để cắt.
     * @param sourcePath Đường dẫn nơi các tệp đang được cắt.
     */
    public void cut(List<FTPFile> filesToCut, String sourcePath) {
        this.files.clear();
        this.files.addAll(filesToCut);
        this.sourcePath = sourcePath;
        this.operation = Operation.CUT;
    }

    /**
     * Xóa clipboard.
     */
    public void clear() {
        this.files.clear();
        this.sourcePath = null;
        this.operation = Operation.NONE;
    }

    /**
     * Kiểm tra xem clipboard có rỗng không.
     * @return true nếu clipboard không có tệp, ngược lại là false.
     */
    public boolean isEmpty() {
        return files.isEmpty();
    }

    // Getters
    public List<FTPFile> getFiles() {
        return new ArrayList<>(files); // Trả về một bản sao để ngăn sửa đổi từ bên ngoài
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public Operation getOperation() {
        return operation;
    }
}