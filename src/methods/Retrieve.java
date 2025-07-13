package methods;

import filemanager.FTPFile;
import ui.FileList;
import ui.ProgressDialog;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Retrieve {

    private static final int BUFFER_SIZE = 4096;

    /**
     * Lấy kích thước của một tệp trên máy chủ FTP bằng lệnh SIZE.
     * @return Kích thước tệp bằng byte, hoặc -1 nếu không thể lấy được.
     */
    private static long getFileSize(PrintWriter controlWriter, BufferedReader controlReader, String filename) throws IOException {
        controlWriter.println("SIZE " + filename);
        String response = controlReader.readLine();
        // Mã 213 là phản hồi thành công cho lệnh SIZE
        if (response != null && response.startsWith("213")) {
            try {
                return Long.parseLong(response.substring(4).trim());
            } catch (NumberFormatException e) {
                return -1; // Không thể phân tích cú pháp kích thước
            }
        }
        // Nếu server không hỗ trợ SIZE, đọc các phản hồi lỗi có thể có để dọn dẹp bộ đệm
        while(controlReader.ready()) controlReader.readLine();
        return -1; // Lệnh SIZE không được hỗ trợ hoặc thất bại
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

        final List<FTPFile> filesToDownload = new ArrayList<>();
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
            List<String> conflictingFiles = new ArrayList<>();
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

            // Tạo và hiển thị dialog tiến trình
            ProgressDialog progressDialog = new ProgressDialog(fileList, "Đang tải xuống...");

            // Sử dụng SwingWorker để thực hiện tải xuống trong nền, tránh làm treo giao diện
            SwingWorker<Integer, Integer> worker = new SwingWorker<>() {
                private final List<String> failedFiles = new ArrayList<>();

                @Override
                protected Integer doInBackground() throws Exception {
                    int successCount = 0;
                    for (int i = 0; i < filesToDownload.size(); i++) {
                        FTPFile file = filesToDownload.get(i);
                        progressDialog.setCurrentFile(file.getName(), i + 1, filesToDownload.size());
                        File localFile = new File(destinationDirectory, file.getName());
                        try {
                            long totalSize = getFileSize(fileList.getControlWriter(), fileList.getControlReader(), file.getName());

                            // 1. Mở kết nối dữ liệu bằng chế độ passive
                            try (Socket dataSocket = Passive.openDataConnection(fileList.getControlReader(), fileList.getControlWriter());
                                 InputStream dataIn = dataSocket.getInputStream();
                                 FileOutputStream fileOut = new FileOutputStream(localFile)) {

                                // 2. Gửi lệnh RETR (Retrieve) qua kết nối điều khiển.
                                fileList.getControlWriter().println("RETR " + file.getName());

                                // 3. Đọc phản hồi ban đầu (ví dụ: 150)
                                String retrResponse = fileList.getControlReader().readLine();
                                if (retrResponse == null || !retrResponse.startsWith("150")) {
                                    throw new IOException("Không thể tải tệp: " + retrResponse);
                                }

                                // 4. Đọc từ kết nối dữ liệu, ghi vào tệp cục bộ và báo cáo tiến trình
                                byte[] buffer = new byte[BUFFER_SIZE];
                                int bytesRead;
                                long totalBytesRead = 0;
                                while ((bytesRead = dataIn.read(buffer)) != -1) {
                                    fileOut.write(buffer, 0, bytesRead);
                                    totalBytesRead += bytesRead;
                                    if (totalSize > 0) {
                                        int progress = (int) ((totalBytesRead * 100) / totalSize);
                                        publish(progress); // Gọi publish trực tiếp từ trong SwingWorker
                                    }
                                }
                                fileOut.flush();
                            }
                            publish(100); // Đảm bảo thanh tiến trình đạt 100%

                            // 5. Đọc phản hồi cuối cùng trên kết nối điều khiển (ví dụ: 226 Transfer complete).
                            String finalResponse = fileList.getControlReader().readLine();
                            if (finalResponse == null || !finalResponse.startsWith("226")) {
                                throw new IOException("Quá trình tải tệp có thể chưa hoàn tất: " + finalResponse);
                            }
                            successCount++;
                        } catch (IOException e) {
                            failedFiles.add(file.getName() + " (" + e.getMessage() + ")");
                        }
                    }
                    return successCount;
                }

                @Override
                protected void process(List<Integer> chunks) {
                    // Lấy giá trị tiến trình cuối cùng được publish
                    if (!chunks.isEmpty()) {
                        int latestProgress = chunks.get(chunks.size() - 1);
                        progressDialog.updateProgress(latestProgress);
                    }
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
                    finally {
                        progressDialog.closeDialog();
                    }
                }
            };
            worker.execute();
            progressDialog.setVisible(true); // Hiển thị dialog sau khi worker bắt đầu
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

    /**
     * Lấy nội dung của một tệp từ máy chủ FTP dưới dạng mảng byte.
     * Phương thức này phù hợp để tải các tệp nhị phân như ảnh.
     *
     * @param controlReader Trình đọc cho kết nối điều khiển.
     * @param controlWriter Trình ghi cho kết nối điều khiển.
     * @param filename      Tên của tệp cần tải về.
     * @return Một mảng byte chứa dữ liệu của tệp.
     * @throws IOException nếu có lỗi trong quá trình giao tiếp FTP.
     */
    public static byte[] getFileBytes(BufferedReader controlReader, PrintWriter controlWriter, String filename) throws IOException {
        // 1. Mở kết nối dữ liệu bằng chế độ passive.
        try (Socket dataSocket = Passive.openDataConnection(controlReader, controlWriter);
             InputStream dataIn = dataSocket.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // 2. Gửi lệnh RETR (Retrieve) qua kết nối điều khiển.
            controlWriter.println("RETR " + filename);

            // 3. Đọc phản hồi ban đầu (ví dụ: 150).
            String retrResponse = controlReader.readLine();
            if (retrResponse == null || !retrResponse.startsWith("150")) {
                throw new IOException("Không thể tải tệp: " + retrResponse);
            }

            // 4. Đọc dữ liệu từ kết nối dữ liệu và ghi vào ByteArrayOutputStream.
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = dataIn.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            // 5. Đọc phản hồi cuối cùng trên kết nối điều khiển (ví dụ: 226 Transfer complete).
            controlReader.readLine(); // Đọc và bỏ qua phản hồi 226

            return baos.toByteArray();
        }
    }
}