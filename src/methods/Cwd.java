package methods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JOptionPane;

import MainWindow.FileList;

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

        // Mã 250 chỉ ra rằng hành động đã thành công.
        return response != null && response.startsWith("250");
    }

    // Thêm phương thức này vào file methods/Cwd.java
    public static void changeDirectoryAndRefresh(FileList fileList, String path) {
        try {
            if (changeDirectory(fileList.getControlWriter(), fileList.getControlReader(), path)) {
                fileList.refreshFileList();
            } else {
                JOptionPane.showMessageDialog(fileList,
                    "Không thể truy cập thư mục '" + path + "'.",
                    "Lỗi Truy Cập", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(fileList, "Lỗi mạng khi đổi thư mục: " + e.getMessage(), "Lỗi Mạng", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
