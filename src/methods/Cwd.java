package methods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Cwd {

    /**
     * Gửi lệnh CWD (Change Working Directory) đến máy chủ FTP.
     *
     * @param writer PrintWriter để gửi lệnh đến máy chủ.
     * @param reader BufferedReader để đọc phản hồi từ máy chủ.
     * @param path   Đường dẫn của thư mục muốn chuyển đến.
     * @return true nếu thay đổi thư mục thành công, false nếu thất bại.
     * @throws IOException Nếu có lỗi giao tiếp mạng.
     */
    public static boolean changeDirectory(PrintWriter writer, BufferedReader reader, String path) throws IOException {
        writer.println("CWD " + path);
        String response = reader.readLine();
        System.out.println("CWD Response: " + response); // Dùng để debug

        // Mã 250 chỉ ra rằng hành động đã thành công.
        return response != null && response.startsWith("250");
    }
}
