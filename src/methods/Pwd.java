package methods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lớp chứa phương thức để lấy thư mục làm việc hiện tại từ máy chủ FTP.
 */
public class Pwd {

    // Pattern để trích xuất đường dẫn từ phản hồi PWD, ví dụ: 257 "/path/to/dir"
    private static final Pattern PWD_PATTERN = Pattern.compile("\"([^\"]*)\"");

    /**
     * Gửi lệnh PWD (Print Working Directory) đến máy chủ FTP.
     *
     * @param writer PrintWriter để gửi lệnh đến máy chủ.
     * @param reader BufferedReader để đọc phản hồi từ máy chủ.
     * @return Đường dẫn của thư mục làm việc hiện tại, hoặc "/" nếu có lỗi.
     * @throws IOException Nếu có lỗi giao tiếp mạng.
     */
    public static String getCurrentDirectory(PrintWriter writer, BufferedReader reader) throws IOException {
        writer.println("PWD");
        String response = reader.readLine();

        // Mã 257 chỉ ra rằng hành động đã thành công và chứa đường dẫn.
        if (response != null && response.startsWith("257")) {
            Matcher matcher = PWD_PATTERN.matcher(response);
            if (matcher.find()) {
                return matcher.group(1); // Trả về đường dẫn được trích xuất
            }
        }
        // Nếu không thể phân tích cú pháp, giả định là thư mục gốc để tránh lỗi
        return "/";
    }
}