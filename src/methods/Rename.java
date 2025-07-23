package methods;

import MainWindow.FileList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.JOptionPane;

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
     * Thực hiện hành động đổi tên và xử lý kết quả.
     * @param fileList Tham chiếu đến FileList để truy cập các thành phần và làm mới.
     * @param oldName Tên tệp cũ.
     * @param newName Tên tệp mới.
     */
    public static void performRename(FileList fileList, String oldName, String newName) {
        try {
            if (renameFile(fileList.getControlWriter(), fileList.getControlReader(), oldName, newName)) {
                // Không cần thông báo thành công, việc làm mới danh sách đã là minh chứng
                fileList.refreshFileList();
            } else {
                JOptionPane.showMessageDialog(fileList, "Không thể đổi tên. Vui lòng kiểm tra lại tên hoặc quyền hạn.", "Lỗi Đổi tên", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(fileList, "Lỗi mạng khi đang đổi tên: " + ex.getMessage(), "Lỗi Mạng", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Bắt đầu quá trình đổi tên cho một hàng cụ thể trong bảng.
     * Phương thức này đặt một cờ để cho phép chỉnh sửa ô, sau đó
     * kích hoạt chế độ chỉnh sửa của JTable.
     * @param fileList Tham chiếu đến FileList để truy cập các thành phần UI.
     */
    public static void initiateRename(FileList fileList) {
        int selectedRow = fileList.getFileTable().getSelectedRow();
        if (selectedRow != -1) {
            fileList.isRenaming = true;
            fileList.renamingRow = selectedRow;

            // Kích hoạt chế độ chỉnh sửa cho ô ở hàng đã chọn và cột "Tên" (index 1)
            fileList.getFileTable().editCellAt(selectedRow, 1);

            // Yêu cầu focus vào trình chỉnh sửa để người dùng có thể nhập ngay lập tức
            java.awt.Component editor = fileList.getFileTable().getEditorComponent();
            if (editor != null) {
                // Đặt con trỏ vào trình chỉnh sửa và chọn tất cả văn bản
                editor.requestFocusInWindow();
                if (editor instanceof javax.swing.JTextField) {
                    ((javax.swing.JTextField) editor).selectAll();
                }
            }
            
            // Cập nhật trạng thái của toolbar để vô hiệu hóa các nút khác
            fileList.getToolbar().updateButtonStates();
        }
    }

    /**
     * Xử lý toàn bộ quy trình đổi tên, bao gồm cả tương tác với người dùng.
     * @param fileList Tham chiếu đến giao diện FileList để truy cập các thành phần UI và trạng thái.
     */
    public static void handleRenameAction(FileList fileList) {
        // Gọi phương thức chuyên dụng trong FileList để bắt đầu quá trình đổi tên
        initiateRename(fileList);
    }
}
