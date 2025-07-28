package methods;

import MainWindow.FileList;
import MainWindow.filemanager.ClipboardManager;
import MainWindow.filemanager.FTPFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Xử lý logic cho hoạt động sao chép.
 */
public class Copy {

    /**
     * Xử lý hành động sao chép các tệp đã chọn vào clipboard.
     * @param fileList Cửa sổ FileList chính.
     */
    public static void handleCopyAction(FileList fileList) {
        int[] selectedRows = fileList.getFileTable().getSelectedRows();
        if (selectedRows.length == 0) {
            return;
        }

        List<FTPFile> filesToCopy = new ArrayList<>();
        for (int row : selectedRows) {
            filesToCopy.add(fileList.getCurrentFiles().get(row));
        }

        ClipboardManager.getInstance().copy(filesToCopy, fileList.getCurrentPath());

        // Sau khi sao chép, cập nhật thanh công cụ để bật nút dán
        fileList.getToolbar().updateButtonStates();
    }
}