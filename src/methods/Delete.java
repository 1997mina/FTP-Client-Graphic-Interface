package methods;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import MainWindow.FileList;
import MainWindow.filemanager.FTPFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Delete {

    /**
     * Gửi lệnh xóa (DELE cho tệp, RMD cho thư mục) đến máy chủ FTP.
     *
     * @param controlWriter Trình ghi để gửi lệnh.
     * @param controlReader Trình đọc để đọc phản hồi.
     * @param fileToDelete  Tệp hoặc thư mục cần xóa.
     * @throws IOException Nếu có lỗi giao tiếp hoặc máy chủ từ chối xóa.
     */
    private static void deleteItem(PrintWriter controlWriter, BufferedReader controlReader, FTPFile fileToDelete) throws IOException {
        // Chọn lệnh phù hợp: RMD cho thư mục, DELE cho tệp
        String command = fileToDelete.isDirectory() ? "RMD " : "DELE ";
        controlWriter.println(command + fileToDelete.getName());

        String response = controlReader.readLine();
        // Mã 250 thường chỉ ra thành công cho DELE và RMD.
        if (response == null || !response.startsWith("250")) {
            throw new IOException("Không thể xóa '" + fileToDelete.getName() + "': " + response);
        }
    }

    /**
     * Xử lý hành động xóa các tệp/thư mục đã chọn từ giao diện người dùng.
     *
     * @param fileList Tham chiếu đến giao diện FileList để truy cập các thành phần UI và trạng thái.
     */
    public static void handleDeleteAction(FileList fileList) {
        int[] selectedRows = fileList.getFileTable().getSelectedRows();

        if (selectedRows.length == 0) {
            return; // Không có gì được chọn, không làm gì cả.
        }

        List<FTPFile> filesToDelete = new ArrayList<>();
        for (int row : selectedRows) {
            filesToDelete.add(fileList.getCurrentFiles().get(row));
        }

        // Tạo thông báo xác nhận để người dùng chắc chắn về hành động của mình.
        String message = "Bạn có chắc chắn muốn xóa " + filesToDelete.size() + " mục đã chọn không?";
        int result = JOptionPane.showConfirmDialog(fileList, message, "Xác nhận Xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            // Sử dụng SwingWorker để thực hiện việc xóa trên một luồng nền, tránh làm treo UI.
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    for (FTPFile file : filesToDelete) {
                        deleteItem(fileList.getControlWriter(), fileList.getControlReader(), file);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get(); // Gọi get() để bắt các exception có thể xảy ra trong doInBackground.
                        JOptionPane.showMessageDialog(fileList, "Đã xóa thành công " + filesToDelete.size() + " mục.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(fileList, "Lỗi khi đang xóa: " + e.getMessage(), "Lỗi Xóa", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    } finally {
                        fileList.refreshFileList(); // Luôn làm mới danh sách tệp để cập nhật giao diện.
                    }
                }
            };
            worker.execute();
        }
    }
}