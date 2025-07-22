package MainWindow;

import MainWindow.filemanager.FTPFile;
import methods.Passive;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class VideoPlayer {

    private static final int BUFFER_SIZE = 4096;

    /**
     * Xử lý việc tải một tệp video về một thư mục tạm và mở nó bằng trình phát mặc định của hệ thống.
     * @param fileList Tham chiếu đến FileList để truy cập trạng thái kết nối.
     * @param fileToOpen Tệp FTP video cần mở.
     */
    public static void openVideo(FileList fileList, FTPFile fileToOpen) {
        // Hỏi người dùng xác nhận trước khi tải
        int result = JOptionPane.showConfirmDialog(
                fileList,
                "Tệp này sẽ được tải về máy và mở bằng trình phát video mặc định.\nBạn có muốn tiếp tục?",
                "Mở Video",
                JOptionPane.YES_NO_OPTION
        );

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(fileList, "Đang tải video...");

        SwingWorker<File, Integer> worker = new SwingWorker<>() {
            @Override
            protected File doInBackground() throws Exception {
                // Tạo một tệp tạm để lưu video
                File tempFile = File.createTempFile("ftp-video-", "." + getFileExtension(fileToOpen.getName()));
                tempFile.deleteOnExit(); // Đánh dấu để xóa khi JVM thoát

                // Lấy kích thước tệp để hiển thị tiến trình
                long totalSize = getFileSize(fileList.getControlWriter(), fileList.getControlReader(), fileToOpen.getName());
                progressDialog.setCurrentFile(fileToOpen.getName(), 1, 1);

                try (Socket dataSocket = Passive.openDataConnection(fileList.getControlReader(), fileList.getControlWriter());
                     InputStream dataIn = dataSocket.getInputStream();
                     FileOutputStream fileOut = new FileOutputStream(tempFile)) {

                    fileList.getControlWriter().println("RETR " + fileToOpen.getName());
                    String retrResponse = fileList.getControlReader().readLine();
                    if (retrResponse == null || !retrResponse.startsWith("150")) {
                        throw new IOException("Không thể tải tệp: " + retrResponse);
                    }

                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    long totalBytesRead = 0;
                    while ((bytesRead = dataIn.read(buffer)) != -1) {
                        fileOut.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        if (totalSize > 0) {
                            int progress = (int) ((totalBytesRead * 100) / totalSize);
                            publish(progress);
                        }
                    }
                    fileOut.flush();
                }
                publish(100);

                String finalResponse = fileList.getControlReader().readLine();
                if (finalResponse == null || !finalResponse.startsWith("226")) {
                    throw new IOException("Quá trình tải tệp có thể chưa hoàn tất: " + finalResponse);
                }

                return tempFile;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                if (!chunks.isEmpty()) {
                    progressDialog.updateProgress(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                progressDialog.closeDialog();
                try {
                    File downloadedFile = get();
                    // Kiểm tra xem Desktop API có được hỗ trợ không
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(downloadedFile);
                    } else {
                        JOptionPane.showMessageDialog(fileList, "Không thể tự động mở tệp. Tệp đã được tải về tại:\n" + downloadedFile.getAbsolutePath(), "Lỗi", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(fileList, "Không thể tải hoặc mở video: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
        progressDialog.setVisible(true);
    }

    /**
     * Lấy phần mở rộng của tệp.
     * @param filename Tên tệp.
     * @return Phần mở rộng của tệp hoặc chuỗi rỗng nếu không có.
     */
    private static String getFileExtension(String filename) {
        int lastIndexOfDot = filename.lastIndexOf(".");
        if (lastIndexOfDot == -1) {
            return ""; // không có phần mở rộng
        }
        return filename.substring(lastIndexOfDot + 1);
    }

    /**
     * Lấy kích thước của một tệp trên máy chủ FTP bằng lệnh SIZE.
     * @return Kích thước tệp bằng byte, hoặc -1 nếu không thể lấy được.
     */
    private static long getFileSize(PrintWriter controlWriter, BufferedReader controlReader, String filename) throws IOException {
        controlWriter.println("SIZE " + filename);
        String response = controlReader.readLine();
        if (response != null && response.startsWith("213")) {
            try {
                return Long.parseLong(response.substring(4).trim());
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        while(controlReader.ready()) controlReader.readLine();
        return -1;
    }
}