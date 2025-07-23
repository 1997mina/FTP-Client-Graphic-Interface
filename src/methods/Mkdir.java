package methods;

import javax.swing.JOptionPane;

import MainWindow.FileList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.table.DefaultTableModel;

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
     * Thực hiện hành động tạo thư mục và làm mới danh sách tệp.
     * Được gọi sau khi người dùng đã nhập tên trong chế độ chỉnh sửa trực tiếp.
     * @param fileList Tham chiếu đến FileList để truy cập các thành phần và làm mới.
     * @param dirName Tên thư mục cần tạo.
     */
    public static void performCreateDirectory(FileList fileList, String dirName) {
        try {
            if (!createDirectory(fileList.getControlWriter(), fileList.getControlReader(), dirName.trim())) {
                JOptionPane.showMessageDialog(fileList, "Không thể tạo thư mục. Tên có thể đã tồn tại hoặc không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(fileList, "Lỗi mạng khi đang tạo thư mục: " + ex.getMessage(), "Lỗi Mạng", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            // Luôn làm mới danh sách, dù thành công hay thất bại, để loại bỏ hàng tạm thời và cập nhật UI.
            fileList.refreshFileList();
        }
    }

	/**
	 * Bắt đầu quá trình tạo thư mục mới bằng cách thêm một hàng tạm thời vào bảng
	 * và đưa nó vào chế độ chỉnh sửa.
	 * @param fileList Tham chiếu đến FileList để truy cập các thành phần UI.
	 */
	public static void initiateCreateDirectory(FileList fileList) {
		if (fileList.editMode != FileList.EditMode.NONE) {
			return; // Đã ở trong một chế độ chỉnh sửa khác
		}

		fileList.editMode = FileList.EditMode.CREATE;
		fileList.editingRow = 0; // Thư mục mới sẽ luôn ở trên cùng

		DefaultTableModel tableModel = fileList.getTableModel();
		// Thêm một hàng trống tạm thời vào đầu bảng
		tableModel.insertRow(0, new Object[]{
			fileList.getSystemIcon(true), // Icon thư mục
			"",                  // Tên trống để người dùng nhập
			"",                  // Kích thước
			""                   // Ngày
		});

		// Cuộn đến và bắt đầu chỉnh sửa hàng mới
		javax.swing.JTable fileTable = fileList.getFileTable();
		fileTable.setRowSelectionInterval(0, 0);
		fileTable.scrollRectToVisible(fileTable.getCellRect(0, 0, true));
		fileTable.editCellAt(0, 1);

		// Yêu cầu focus vào trình chỉnh sửa
		java.awt.Component editor = fileTable.getEditorComponent();
		if (editor != null) {
			editor.requestFocusInWindow();
		}

		fileList.getToolbar().updateButtonStates(); // Vô hiệu hóa các nút khác
	}
}