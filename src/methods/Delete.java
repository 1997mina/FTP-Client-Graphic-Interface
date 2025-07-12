package methods;

import filemanager.FTPFile;
import ui.FileList;

import javax.swing.JOptionPane;
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

    /**
     * Xử lý toàn bộ quy trình xóa tệp, bao gồm cả tương tác với người dùng.
     * @param fileList Tham chiếu đến giao diện FileList để truy cập các thành phần UI và trạng thái.
     */
    public static void handleDeleteAction(FileList fileList) {
        int selectedRow = fileList.getFileTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(fileList, "Vui lòng chọn một tệp để xóa.", "Chưa chọn tệp", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        FTPFile fileToDelete = fileList.getCurrentFiles().get(selectedRow);

        if (fileToDelete.isDirectory()) {
            JOptionPane.showMessageDialog(fileList, "Không thể xóa thư mục bằng chức năng này.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(fileList, "Bạn có chắc chắn muốn xóa tệp '" + fileToDelete.getName() + "' không?\nHành động này không thể hoàn tác.", "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (deleteFile(fileList.getControlWriter(), fileList.getControlReader(), fileToDelete.getName())) {
                    JOptionPane.showMessageDialog(fileList, "Đã xóa tệp thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    fileList.refreshFileList(); // Tải lại danh sách tệp
                } else {
                    JOptionPane.showMessageDialog(fileList, "Không thể xóa tệp. Vui lòng kiểm tra quyền hạn của bạn.", "Lỗi Xóa", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(fileList, "Lỗi mạng khi đang xóa tệp: " + ex.getMessage(), "Lỗi Mạng", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}
