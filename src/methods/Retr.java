package methods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Retr {
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
        try (Socket dataSocket = Pasv.openDataConnection(controlReader, controlWriter);
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