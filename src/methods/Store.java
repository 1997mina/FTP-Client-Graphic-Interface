package methods;

import ui.FileList;
import ui.ProgressDialog;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class Store {
    private static final int BUFFER_SIZE = 4096;

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

            ProgressDialog progressDialog = new ProgressDialog(fileList, "Đang tải lên...");

            // Sử dụng SwingWorker để thực hiện tải lên trong nền
            SwingWorker<Void, Integer> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    for (int i = 0; i < selectedFiles.length; i++) {
                        File file = selectedFiles[i];
                        progressDialog.setCurrentFile(file.getName(), i + 1, selectedFiles.length);

                        // 1. Mở kết nối dữ liệu bằng chế độ passive
                        try (Socket dataSocket = Passive.openDataConnection(fileList.getControlReader(), fileList.getControlWriter());
                             OutputStream dataOut = dataSocket.getOutputStream();
                             FileInputStream fileIn = new FileInputStream(file)) {

                            // 2. Gửi lệnh STOR (Store) qua kết nối điều khiển.
                            fileList.getControlWriter().println("STOR " + file.getName());

                            // 3. Đọc phản hồi ban đầu (ví dụ: 150)
                            String storResponse = fileList.getControlReader().readLine();
                            if (storResponse == null || !storResponse.startsWith("150")) {
                                throw new IOException("Không thể bắt đầu tải lên tệp: " + storResponse);
                            }

                            // 4. Đọc tệp cục bộ, ghi vào kết nối dữ liệu và báo cáo tiến trình
                            byte[] buffer = new byte[BUFFER_SIZE];
                            int bytesRead;
                            long totalBytesWritten = 0;
                            long totalSize = file.length();
                            while ((bytesRead = fileIn.read(buffer)) != -1) {
                                dataOut.write(buffer, 0, bytesRead);
                                totalBytesWritten += bytesRead;
                                if (totalSize > 0) {
                                    int progress = (int) ((totalBytesWritten * 100) / totalSize);
                                    publish(progress); // Gọi publish trực tiếp từ trong SwingWorker
                                }
                            }
                            dataOut.flush(); // Đảm bảo tất cả dữ liệu đã được gửi đi
                        }
                        publish(100); // Đảm bảo thanh tiến trình đạt 100%

                        // 5. Đọc phản hồi cuối cùng trên kết nối điều khiển (ví dụ: 226 Transfer complete).
                        String finalResponse = fileList.getControlReader().readLine();
                        if (finalResponse == null || !finalResponse.startsWith("226")) {
                            throw new IOException("Tải lên tệp có thể không hoàn tất: " + finalResponse);
                        }
                    }
                    return null;
                }

                @Override
                protected void process(List<Integer> chunks) {
                    if (!chunks.isEmpty()) {
                        int latestProgress = chunks.get(chunks.size() - 1);
                        progressDialog.updateProgress(latestProgress);
                    }
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
                    } finally {
                        progressDialog.closeDialog();
                    }
                }
            };
            worker.execute();
            progressDialog.setVisible(true);
        }
    }
}
