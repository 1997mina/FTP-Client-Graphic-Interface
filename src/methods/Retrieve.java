package methods;

import filemanager.FTPFile;
import ui.FileList;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class Retrieve {

    private static final int BUFFER_SIZE = 4096;

    /**
     * Tải một tệp từ máy chủ FTP về máy cục bộ bằng lệnh RETR.
     *
     * @param controlWriter  Trình ghi cho kết nối điều khiển.
     * @param controlReader  Trình đọc cho kết nối điều khiển.
     * @param remoteFileName Tên tệp trên máy chủ.
     * @param localFile      Tệp cục bộ để lưu dữ liệu vào.
     * @throws IOException nếu có lỗi trong quá trình giao tiếp FTP hoặc ghi tệp cục bộ.
     */
    private static void downloadFile(PrintWriter controlWriter, BufferedReader controlReader, String remoteFileName, File localFile) throws IOException {
        // 1. Mở kết nối dữ liệu bằng chế độ passive.
        try (Socket dataSocket = Passive.openDataConnection(controlReader, controlWriter);
             InputStream dataIn = dataSocket.getInputStream();
             FileOutputStream fileOut = new FileOutputStream(localFile)) {

            // 2. Gửi lệnh RETR (Retrieve) qua kết nối điều khiển.
            controlWriter.println("RETR " + remoteFileName);

            // 3. Đọc phản hồi ban đầu (ví dụ: 150).
            String retrResponse = controlReader.readLine();
            if (retrResponse == null || !retrResponse.startsWith("150")) {
                throw new IOException("Không thể tải tệp: " + retrResponse);
            }

            // 4. Đọc từ kết nối dữ liệu và ghi vào tệp cục bộ.
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = dataIn.read(buffer)) != -1) {
                fileOut.write(buffer, 0, bytesRead);
            }
            fileOut.flush();
        }

        // 5. Đọc phản hồi cuối cùng trên kết nối điều khiển (ví dụ: 226 Transfer complete).
        String finalResponse = controlReader.readLine();
        if (finalResponse == null || !finalResponse.startsWith("226")) {
            throw new IOException("Quá trình tải tệp có thể chưa hoàn tất: " + finalResponse);
        }
    }

    /**
     * Xử lý toàn bộ quy trình tải xuống, bao gồm cả việc chọn nơi lưu và thực hiện trên luồng nền.
     * @param fileList Tham chiếu đến giao diện FileList để truy cập các thành phần UI và trạng thái.
     */
    public static void handleDownloadAction(FileList fileList) {
        int[] selectedRows = fileList.getFileTable().getSelectedRows();
        if (selectedRows.length == 0) {
            return;
        }

        final java.util.List<FTPFile> filesToDownload = new java.util.ArrayList<>();
        for (int row : selectedRows) {
            FTPFile selectedFile = fileList.getCurrentFiles().get(row);
            if (!selectedFile.isDirectory()) {
                filesToDownload.add(selectedFile);
            }
        }

        if (filesToDownload.isEmpty()) {
            JOptionPane.showMessageDialog(fileList, "Không có tệp nào trong lựa chọn của bạn để tải xuống.", "Không có tệp", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn thư mục để lưu " + filesToDownload.size() + " tệp");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int result = fileChooser.showOpenDialog(fileList);
        if (result == JFileChooser.APPROVE_OPTION) {
            final File destinationDirectory = fileChooser.getSelectedFile();

            // Kiểm tra các tệp có thể bị ghi đè trước khi bắt đầu
            java.util.List<String> conflictingFiles = new java.util.ArrayList<>();
            for (FTPFile ftpFile : filesToDownload) {
                if (new File(destinationDirectory, ftpFile.getName()).exists()) {
                    conflictingFiles.add(ftpFile.getName());
                }
            }

            if (!conflictingFiles.isEmpty()) {
                String message = "Các tệp sau đã tồn tại và sẽ bị ghi đè:\n" + String.join("\n", conflictingFiles) + "\n\nBạn có muốn tiếp tục không?";
                int overwriteResult = JOptionPane.showConfirmDialog(fileList, message, "Xác nhận Ghi đè", JOptionPane.YES_NO_OPTION);
                if (overwriteResult == JOptionPane.NO_OPTION) {
                    return; // Người dùng hủy bỏ thao tác
                }
            }

            // Sử dụng SwingWorker để thực hiện tải xuống trong nền, tránh làm treo giao diện
            SwingWorker<Integer, Void> worker = new SwingWorker<>() {
                private final java.util.List<String> failedFiles = new java.util.ArrayList<>();

                @Override
                protected Integer doInBackground() throws Exception {
                    int successCount = 0;
                    for (FTPFile file : filesToDownload) {
                        try {
                            File localFile = new File(destinationDirectory, file.getName());
                            downloadFile(fileList.getControlWriter(), fileList.getControlReader(), file.getName(), localFile);
                            successCount++;
                        } catch (IOException e) {
                            failedFiles.add(file.getName() + " (" + e.getMessage() + ")");
                        }
                    }
                    return successCount;
                }

                @Override
                protected void done() {
                    try {
                        int successCount = get(); // Lấy kết quả từ doInBackground
                        String summary = "Tải xuống hoàn tất.\nThành công: " + successCount + "/" + filesToDownload.size() + " tệp.";
                        if (!failedFiles.isEmpty()) {
                            summary += "\n\nCác tệp sau đã gặp lỗi:\n" + String.join("\n", failedFiles);
                            JOptionPane.showMessageDialog(fileList, summary, "Tải xuống hoàn tất với lỗi", JOptionPane.WARNING_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(fileList, summary, "Tải xuống thành công", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(fileList, "Đã xảy ra lỗi không mong muốn: " + e.getMessage(), "Lỗi nghiêm trọng", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }
            };
            worker.execute();
        }
    }

    /**
     * Lấy nội dung của một tệp từ máy chủ FTP bằng lệnh RETR.
     *
     * @param controlReader Trình đọc cho kết nối điều khiển.
     * @param controlWriter Trình ghi cho kết nối điều khiển.
     * @param filename      Tên của tệp cần tải về.
     * @return Một chuỗi chứa nội dung của tệp.
     * @throws IOException nếu có lỗi trong quá trình giao tiếp FTP.
     */
    public static String getFileContent(BufferedReader controlReader, PrintWriter controlWriter, String filename) throws IOException {
        // 1. Mở kết nối dữ liệu bằng chế độ passive.
        try (Socket dataSocket = Passive.openDataConnection(controlReader, controlWriter);
             BufferedReader dataReader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()))) {

            // 2. Gửi lệnh RETR (Retrieve) qua kết nối điều khiển.
            controlWriter.println("RETR " + filename);

            // 3. Đọc phản hồi ban đầu (ví dụ: 150).
            String retrResponse = controlReader.readLine();
            if (retrResponse == null || !retrResponse.startsWith("150")) {
                throw new IOException("Không thể tải tệp: " + retrResponse);
            }

            // 4. Đọc nội dung tệp từ kết nối dữ liệu.
            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = dataReader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }

            // 5. Đọc phản hồi cuối cùng trên kết nối điều khiển (ví dụ: 226 Transfer complete).
            controlReader.readLine(); // Đọc và bỏ qua phản hồi 226

            return fileContent.toString();
        }
    }
}