package methods;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import MainWindow.FileList;
import MainWindow.filemanager.FTPFile;
import MainWindow.filemanager.FTPFileParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class Delete {

    /**
     * Gửi lệnh xóa (DELE cho tệp, RMD cho thư mục) đến máy chủ FTP.
     *
     * @param writer Trình ghi để gửi lệnh.
     * @param reader Trình đọc để đọc phản hồi.
     * @param filename  Tên tệp cần xóa.
     * @throws IOException Nếu có lỗi giao tiếp hoặc máy chủ từ chối xóa.
     */
    private static void deleteFile(PrintWriter writer, BufferedReader reader, String filename) throws IOException {
        writer.println("DELE " + filename);
        String response = reader.readLine();
        // Mã 250 thường chỉ ra thành công cho DELE và RMD.
        if (response == null || !response.startsWith("250")) {
            throw new IOException("Không thể xóa tệp '" + filename + "': " + response);
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

        // Tạo thông báo xác nhận để người dùng chắc chắn về hành động của mình.
        String message;
        if (selectedRows.length == 1) {
            message = "Bạn có chắc chắn muốn xóa \"" + fileList.getCurrentFiles().get(selectedRows[0]).getName() + "\"?";
        } else {
            message = "Bạn có chắc chắn muốn xóa " + selectedRows.length + " mục đã chọn không?";
        }
        int result = JOptionPane.showConfirmDialog(fileList, message, "Xác nhận Xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            // Sử dụng SwingWorker để thực hiện việc xóa trên một luồng nền, tránh làm treo UI.
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    PrintWriter writer = fileList.getControlWriter();
                    BufferedReader reader = fileList.getControlReader();

                    for (int row : selectedRows) {
                        FTPFile fileToDelete = fileList.getCurrentFiles().get(row);
                        if (fileToDelete.isDirectory()) {
                            // Nếu là thư mục, thực hiện xóa đệ quy
                            recursivelyDeleteDirectory(writer, reader, fileToDelete.getName());
                        } else {
                            // Nếu là tệp, chỉ cần xóa nó
                            deleteFile(writer, reader, fileToDelete.getName());
                        }
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get(); // Gọi get() để bắt các exception có thể xảy ra trong doInBackground.
                        JOptionPane.showMessageDialog(fileList, "Đã xóa thành công " + selectedRows.length + " mục.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e) {
                        // Lấy nguyên nhân gốc của lỗi để hiển thị thông báo chính xác hơn
                        Throwable cause = e.getCause() != null ? e.getCause() : e;
                        JOptionPane.showMessageDialog(fileList, "Lỗi khi đang xóa: " + cause.getMessage(), "Lỗi Xóa", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    } finally {
                        fileList.refreshFileList(); // Luôn làm mới danh sách tệp để cập nhật giao diện.
                    }
                }
            }.execute();
        }
    }

    /**
     * Thực hiện xóa đệ quy một thư mục trên máy chủ FTP.
     * @param writer Trình ghi để gửi lệnh.
     * @param reader Trình đọc để đọc phản hồi.
     * @param path   Đường dẫn của thư mục cần xóa.
     * @throws IOException Nếu có lỗi xảy ra trong quá trình xóa.
     */
    private static void recursivelyDeleteDirectory(PrintWriter writer, BufferedReader reader, String path) throws IOException {
        // 1. Đi vào thư mục cần xóa
        if (!Cwd.changeDirectory(writer, reader, path)) {
            throw new IOException("Không thể truy cập thư mục: " + path);
        }

        // 2. Lấy danh sách các mục bên trong
        String listData = methods.List.getFileList(reader, writer);
        List<FTPFile> files = FTPFileParser.parse(listData);

        // 3. Xóa từng mục
        for (FTPFile file : files) {
            if (file.getName().equals(".") || file.getName().equals("..")) continue;

            if (file.isDirectory()) {
                recursivelyDeleteDirectory(writer, reader, file.getName());
            } else {
                deleteFile(writer, reader, file.getName());
            }
        }

        // 4. Quay lại thư mục cha
        if (!Cdup.goUp(writer, reader)) {
            throw new IOException("Không thể quay lại thư mục cha từ " + path);
        }

        // 5. Xóa thư mục (giờ đã rỗng)
        writer.println("RMD " + path);
        String rmdResponse = reader.readLine();
        if (rmdResponse == null || !rmdResponse.startsWith("250")) {
            throw new IOException("Không thể xóa thư mục '" + path + "'. Phản hồi: " + rmdResponse);
        }
    }
}