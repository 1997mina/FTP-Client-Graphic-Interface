package methods;

import MainWindow.FileList;
import MainWindow.ProgressDialog;
import MainWindow.filemanager.ClipboardManager;
import MainWindow.filemanager.FTPFile;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Xử lý logic cho hoạt động dán (paste).
 */
public class Paste {

    /**
     * Bắt đầu hành động dán các tệp từ clipboard vào thư mục hiện tại.
     * @param fileList Cửa sổ FileList chính.
     */
    public static void handlePasteAction(FileList fileList) {
        ClipboardManager clipboard = ClipboardManager.getInstance();
        if (clipboard.isEmpty()) {
            return;
        }

        String sourcePath = clipboard.getSourcePath();
        String destinationPath = fileList.getCurrentPath();

        // Ngăn chặn việc cắt và dán vào cùng một thư mục (hành động vô nghĩa)
        if (clipboard.getOperation() == ClipboardManager.Operation.CUT && sourcePath.equals(destinationPath)) {
            return;
        }

        List<FTPFile> filesToPaste = clipboard.getFiles();

        // Tạo và hiển thị dialog tiến trình
        ProgressDialog progressDialog = new ProgressDialog(fileList, "Đang dán...");

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            private final List<String> errors = new ArrayList<>();

            @Override
            protected Void doInBackground() throws Exception {
                ClipboardManager.Operation operation = clipboard.getOperation();

                // Lấy danh sách tên tệp hiện có ở thư mục đích để kiểm tra xung đột
                List<String> existingFileNames = fileList.getCurrentFiles().stream()
                        .map(FTPFile::getName)
                        .collect(Collectors.toList());

                for (int i = 0; i < filesToPaste.size(); i++) {
                    FTPFile fileToPaste = filesToPaste.get(i);
                    String originalName = fileToPaste.getName();

                    // Cập nhật UI
                    progressDialog.setCurrentFile(originalName, i + 1, filesToPaste.size());
                    progressDialog.updateProgress(0);

                    try {
                        // Xử lý xung đột tên tệp
                        String finalName = handleNameConflict(originalName, existingFileNames);

                        if (operation == ClipboardManager.Operation.CUT) {
                            // --- LOGIC CHO VIỆC CẮT (DI CHUYỂN) ---
                            String sourceFile = sourcePath.equals("/") ? "/" + originalName : sourcePath + "/" + originalName;
                            String destinationFile = destinationPath.equals("/") ? "/" + finalName : destinationPath + "/" + finalName;

                            if (!Rename.renameFile(fileList.getControlWriter(), fileList.getControlReader(), sourceFile, destinationFile)) {
                                throw new IOException("Không thể di chuyển: " + originalName);
                            }
                            publish("Đã di chuyển: " + originalName);
                            progressDialog.updateProgress(100);

                        } else { // operation == ClipboardManager.Operation.COPY
                            // --- LOGIC CHO VIỆC SAO CHÉP (TẢI XUỐNG/TẢI LÊN) ---
                            if (fileToPaste.isDirectory()) {
                                // TODO: Triển khai sao chép thư mục đệ quy trong tương lai
                                publish("Bỏ qua thư mục: " + finalName);
                                progressDialog.updateProgress(100);
                                continue;
                            }

                            // Bước 1: Tải tệp nguồn vào bộ nhớ (dưới dạng byte array)
                            if (!Cwd.changeDirectory(fileList.getControlWriter(), fileList.getControlReader(), sourcePath)) {
                                throw new IOException("Không thể truy cập thư mục nguồn: " + sourcePath);
                            }
                            byte[] fileData = Retrieve.getFileBytes(fileList.getControlReader(), fileList.getControlWriter(), originalName);
                            publish("Đã tải xuống: " + originalName);
                            progressDialog.updateProgress(50);

                            // Bước 2: Tải tệp từ bộ nhớ lên thư mục đích
                            if (!Cwd.changeDirectory(fileList.getControlWriter(), fileList.getControlReader(), destinationPath)) {
                                throw new IOException("Không thể truy cập thư mục đích: " + destinationPath);
                            }
                            Store.uploadFileFromBytes(fileList.getControlReader(), fileList.getControlWriter(), fileData, finalName);
                            publish("Đã tải lên: " + finalName);
                            progressDialog.updateProgress(100);
                        }

                        // Thêm tên mới vào danh sách để các lần lặp sau biết
                        existingFileNames.add(finalName);

                    } catch (IOException e) {
                        errors.add("Lỗi khi dán '" + originalName + "': " + e.getMessage());
                    }
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                // Có thể cập nhật một nhãn trạng thái chi tiết ở đây nếu muốn
            }

            @Override
            protected void done() {
                progressDialog.closeDialog();
                if (!errors.isEmpty()) {
                    String errorMessages = String.join("\n", errors);
                    JOptionPane.showMessageDialog(fileList, "Đã xảy ra lỗi trong quá trình dán:\n" + errorMessages, "Lỗi", JOptionPane.ERROR_MESSAGE);
                }

                // Nếu hành động là CẮT, hãy xóa clipboard để ngăn việc dán lại.
                if (clipboard.getOperation() == ClipboardManager.Operation.CUT) {
                    clipboard.clear();
                }

                // Làm mới danh sách tệp để hiển thị các tệp vừa được dán
                fileList.refreshFileList();
                // Cập nhật lại các nút, đặc biệt là nút Dán
                fileList.getToolbar().updateButtonStates();
            }
        };

        worker.execute();
        progressDialog.setVisible(true);
    }

    /**
     * Kiểm tra xem tên tệp có tồn tại không và tạo tên mới nếu cần.
     * Ví dụ: "file.txt" -> "file (copy).txt" -> "file (copy 2).txt"
     */
    private static String handleNameConflict(String originalName, List<String> existingNames) {
        if (!existingNames.contains(originalName)) {
            return originalName;
        }

        String nameWithoutExt = originalName.contains(".") ? originalName.substring(0, originalName.lastIndexOf('.')) : originalName;
        String ext = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')) : "";

        String newName = nameWithoutExt + " (copy)" + ext;
        if (!existingNames.contains(newName)) {
            return newName;
        }

        int count = 2;
        while (true) {
            newName = nameWithoutExt + " (copy " + count + ")" + ext;
            if (!existingNames.contains(newName)) {
                return newName;
            }
            count++;
        }
    }
}