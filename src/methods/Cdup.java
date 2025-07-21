package methods;

import javax.swing.JOptionPane;

import MainWindow.FileList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Cdup {

    /**
     * Gửi lệnh CDUP (Change to Parent Directory) đến máy chủ FTP.
     *
     * @param writer PrintWriter để gửi lệnh đến máy chủ.
     * @param reader BufferedReader để đọc phản hồi từ máy chủ.
     * @return true nếu thay đổi thư mục thành công, false nếu thất bại.
     * @throws IOException Nếu có lỗi giao tiếp mạng.
     */
    private static boolean goUp(PrintWriter writer, BufferedReader reader) throws IOException {
        writer.println("CDUP");
        String response = reader.readLine();

        // Mã 250 hoặc 200 đều chỉ ra rằng hành động đã thành công.
        return response != null && (response.startsWith("250") || response.startsWith("200"));
    }

    /**
     * Xử lý hành động quay lại thư mục cha khi người dùng nhấn nút.
     * @param fileList Tham chiếu đến giao diện FileList để làm mới danh sách tệp.
     */
    public static void handleBackAction(FileList fileList) {
        try {
            if (goUp(fileList.getControlWriter(), fileList.getControlReader())) {
                fileList.refreshFileList();
            }
            // Không cần hiển thị lỗi nếu thất bại, vì có thể người dùng đã ở thư mục gốc.
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(fileList, "Lỗi mạng khi thực hiện lệnh quay lại: " + ex.getMessage(), "Lỗi Mạng", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}