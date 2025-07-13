package methods;

import ui.FileList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class Stor {
    private static final int BUFFER_SIZE = 4096;

    /**
     * Tải một tệp cục bộ lên máy chủ FTP bằng lệnh STOR.
     *
     * @param controlWriter Trình ghi cho kết nối điều khiển.
     * @param controlReader Trình đọc cho kết nối điều khiển.
     * @param localFile     Tệp cục bộ cần tải lên.
     * @throws IOException nếu có lỗi trong quá trình giao tiếp FTP hoặc đọc tệp cục bộ.
     */
    private static void uploadFile(PrintWriter controlWriter, BufferedReader controlReader, File localFile) throws IOException {
        // 1. Mở kết nối dữ liệu bằng chế độ passive.
        try (Socket dataSocket = Pasv.openDataConnection(controlReader, controlWriter);
             OutputStream dataOut = dataSocket.getOutputStream();
             FileInputStream fileIn = new FileInputStream(localFile)) {

            // 2. Gửi lệnh STOR (Store) qua kết nối điều khiển.
            controlWriter.println("STOR " + localFile.getName());

            // 3. Đọc phản hồi ban đầu (ví dụ: 150 Opening data connection).
            String storResponse = controlReader.readLine();
            if (storResponse == null || !storResponse.startsWith("150")) {
                throw new IOException("Không thể bắt đầu tải lên tệp: " + storResponse);
            }

            // 4. Đọc tệp cục bộ và ghi vào kết nối dữ liệu.
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fileIn.read(buffer)) != -1) {
                dataOut.write(buffer, 0, bytesRead);
            }
            dataOut.flush(); // Đảm bảo tất cả dữ liệu đã được gửi đi
        }

        // 5. Đọc phản hồi cuối cùng trên kết nối điều khiển (ví dụ: 226 Transfer complete).
        String finalResponse = controlReader.readLine();
        if (finalResponse == null || !finalResponse.startsWith("226")) {
            throw new IOException("Tải lên tệp có thể không hoàn tất: " + finalResponse);
        }
    }

    /**
     * Xử lý toàn bộ quy trình tải lên, bao gồm cả việc chọn tệp và thực hiện trên luồng nền.
     * @param fileList Tham chiếu đến giao diện FileList để truy cập các thành phần UI và trạng thái.
     */
    public static void handleUploadAction(FileList fileList) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn tệp để tải lên");
        fileChooser.setMultiSelectionEnabled(true); // Cho phép chọn nhiều tệp

        int result = fileChooser.showOpenDialog(fileList);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();

            // Sử dụng SwingWorker để thực hiện tải lên trong nền
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    for (File file : selectedFiles) {
                        uploadFile(fileList.getControlWriter(), fileList.getControlReader(), file);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get(); // Gọi get() để bắt các exception có thể xảy ra trong doInBackground
                        JOptionPane.showMessageDialog(fileList, "Đã tải lên thành công " + selectedFiles.length + " tệp.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        fileList.refreshFileList(); // Tải lại danh sách tệp
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(fileList, "Lỗi khi đang tải lên tệp: " + e.getMessage(), "Lỗi Tải lên", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                        fileList.refreshFileList(); // Vẫn tải lại để xem trạng thái hiện tại
                    }
                }
            };
            worker.execute();
        }
    }
}
