package methods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class List {
    /**
     * Lấy danh sách tệp từ máy chủ FTP bằng lệnh LIST.
     * Phương thức này xử lý việc vào chế độ passive, mở kết nối dữ liệu,
     * và đọc danh sách.
     *
     * @param controlReader Trình đọc cho kết nối điều khiển.
     * @param controlWriter Trình ghi cho kết nối điều khiển.
     * @return Một chuỗi chứa danh sách tệp.
     * @throws IOException nếu có lỗi trong quá trình giao tiếp FTP.
     */
    public static String getFileList(BufferedReader controlReader, PrintWriter controlWriter) throws IOException {
        // 1. Mở kết nối dữ liệu bằng chế độ passive.
        try (Socket dataSocket = Passive.openDataConnection(controlReader, controlWriter);
             BufferedReader dataReader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()))) {

            // 2. Gửi lệnh LIST qua kết nối điều khiển.
            controlWriter.println("LIST");

            // 3. Đọc phản hồi ban đầu (ví dụ: 150).
            String listResponse = controlReader.readLine();
            if (listResponse == null || !listResponse.startsWith("150")) {
                throw new IOException("Không thể lấy danh sách tệp: " + listResponse);
            }

            // 4. Đọc danh sách tệp từ kết nối dữ liệu.
            StringBuilder fileList = new StringBuilder();
            String line;
            while ((line = dataReader.readLine()) != null) {
                fileList.append(line).append("\n");
            }

            // 5. Đọc phản hồi cuối cùng trên kết nối điều khiển (ví dụ: 226).
            controlReader.readLine(); // Đọc và bỏ qua phản hồi 226

            return fileList.toString();
        }
    }
}
