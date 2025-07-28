package methods;

import MainWindow.FileList;
import MainWindow.filemanager.ClipboardManager;
import MainWindow.filemanager.FTPFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Xử lý logic cho hoạt động cắt (cut).
 */
public class Cut {

    /**
     * Xử lý hành động cắt các tệp đã chọn vào clipboard.
     * @param fileList Cửa sổ FileList chính.
     */
    public static void handleCutAction(FileList fileList) {
        int[] selectedRows = fileList.getFileTable().getSelectedRows();
        if (selectedRows.length == 0) {
            return;
        }

        List<FTPFile> filesToCut = new ArrayList<>();
        for (int row : selectedRows) {
            filesToCut.add(fileList.getCurrentFiles().get(row));
        }

        ClipboardManager.getInstance().cut(filesToCut, fileList.getCurrentPath());
        fileList.getToolbar().updateButtonStates();
    }
}