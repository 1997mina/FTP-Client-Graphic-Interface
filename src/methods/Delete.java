package methods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Delete {
    /**
     * Gửi lệnh DELE để xóa một tệp trên máy chủ FTP.
     *
     * @param writer   PrintWriter để gửi lệnh đến máy chủ.
     * @param reader   BufferedReader để đọc phản hồi từ máy chủ.
     * @param fileName Tên của tệp cần xóa.
     * @return true nếu xóa thành công, false nếu thất bại.
     * @throws IOException Nếu có lỗi giao tiếp mạng.
     */
    public static boolean deleteFile(PrintWriter writer, BufferedReader reader, String fileName) throws IOException {
        writer.println("DELE " + fileName);
        String response = reader.readLine();

        // Mã 250 chỉ ra rằng hành động đã thành công.
        return response != null && response.startsWith("250");
    }
}
