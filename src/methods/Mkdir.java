package methods;

import javax.swing.JOptionPane;

import MainWindow.FileList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Mkdir {

    /**
     * Gửi lệnh MKD (Make Directory) đến máy chủ FTP.
     *
     * @param writer  PrintWriter để gửi lệnh.
     * @param reader  BufferedReader để đọc phản hồi.
     * @param dirName Tên của thư mục cần tạo.
     * @return true nếu tạo thành công, false nếu thất bại.
     * @throws IOException Nếu có lỗi giao tiếp mạng.
     */
    private static boolean createDirectory(PrintWriter writer, BufferedReader reader, String dirName) throws IOException {
        writer.println("MKD " + dirName);
        String response = reader.readLine();
        // Mã 257 thường chỉ ra thành công.
        if (response != null && response.startsWith("257")) {
            return true;
        } else {
            System.err.println("Không thể tạo thư mục '" + dirName + "'. Phản hồi: " + response);
            return false;
        }
    }

    /**
     * Xử lý hành động tạo thư mục mới, bao gồm cả việc hỏi tên từ người dùng.
     *
     * @param fileList Tham chiếu đến giao diện FileList để làm mới danh sách.
     */
    public static void handleCreateDirectoryAction(FileList fileList) {
        String dirName = JOptionPane.showInputDialog(fileList, "Nhập tên thư mục mới:", "Tạo thư mục", JOptionPane.PLAIN_MESSAGE);

        if (dirName == null || dirName.trim().isEmpty()) {
            return; // Người dùng hủy hoặc không nhập gì.
        }

        try {
            if (createDirectory(fileList.getControlWriter(), fileList.getControlReader(), dirName.trim())) {
                fileList.refreshFileList();
            } else {
                JOptionPane.showMessageDialog(fileList, "Không thể tạo thư mục. Tên có thể đã tồn tại hoặc không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(fileList, "Lỗi mạng khi đang tạo thư mục: " + ex.getMessage(), "Lỗi Mạng", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}