package methods;

import filemanager.FTPFile;
import ui.FileList;

import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Rename {
    /**
     * Gửi lệnh RNFR và RNTO để đổi tên một tệp hoặc thư mục trên máy chủ FTP.
     *
     * @param writer   PrintWriter để gửi lệnh đến máy chủ.
     * @param reader   BufferedReader để đọc phản hồi từ máy chủ.
     * @param fromName Tên hiện tại của tệp/thư mục.
     * @param toName   Tên mới của tệp/thư mục.
     * @return true nếu đổi tên thành công, false nếu thất bại.
     * @throws IOException Nếu có lỗi giao tiếp mạng.
     */
    public static boolean renameFile(PrintWriter writer, BufferedReader reader, String fromName, String toName) throws IOException {
        // 1. Gửi lệnh RNFR (Rename From)
        writer.println("RNFR " + fromName);
        String rnfrResponse = reader.readLine();

        // Máy chủ sẽ trả về mã 350 nếu tệp tồn tại và sẵn sàng để đổi tên.
        if (rnfrResponse == null || !rnfrResponse.startsWith("350")) {
            System.err.println("Lệnh RNFR thất bại cho '" + fromName + "'. Phản hồi: " + rnfrResponse);
            return false; // Thất bại, có thể tệp không tồn tại hoặc không có quyền.
        }

        // 2. Gửi lệnh RNTO (Rename To)
        writer.println("RNTO " + toName);
        String rntoResponse = reader.readLine();

        // Mã 250 chỉ ra rằng hành động đổi tên đã hoàn tất thành công.
        return rntoResponse != null && rntoResponse.startsWith("250");
    }

    /**
     * Xử lý toàn bộ quy trình đổi tên, bao gồm cả tương tác với người dùng.
     * @param fileList Tham chiếu đến giao diện FileList để truy cập các thành phần UI và trạng thái.
     */
    public static void handleRenameAction(FileList fileList) {
        int selectedRow = fileList.getFileTable().getSelectedRow();
        if (selectedRow == -1) {
            // Điều này không nên xảy ra vì nút đã bị vô hiệu hóa, nhưng để an toàn
            return;
        }

        FTPFile fileToRename = fileList.getCurrentFiles().get(selectedRow);
        String oldName = fileToRename.getName();

        // Hiển thị hộp thoại nhập liệu với tên cũ làm giá trị mặc định
        String newName = JOptionPane.showInputDialog(fileList, "Nhập tên mới:", oldName);

        // Kiểm tra nếu người dùng hủy, không nhập gì, hoặc không thay đổi tên
        if (newName == null || newName.trim().isEmpty() || newName.trim().equals(oldName)) {
            return;
        }

        try {
            if (renameFile(fileList.getControlWriter(), fileList.getControlReader(), oldName, newName.trim())) {
                JOptionPane.showMessageDialog(fileList, "Đã đổi tên thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                fileList.refreshFileList(); // Tải lại danh sách
            } else {
                JOptionPane.showMessageDialog(fileList, "Không thể đổi tên. Vui lòng kiểm tra lại tên hoặc quyền hạn.", "Lỗi Đổi tên", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(fileList, "Lỗi mạng khi đang đổi tên: " + ex.getMessage(), "Lỗi Mạng", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
