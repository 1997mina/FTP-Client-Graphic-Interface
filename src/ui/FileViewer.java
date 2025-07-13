package ui;

import methods.Retr;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.PrintWriter;

/**
 * Một cửa sổ đơn giản để hiển thị nội dung của một tệp văn bản.
 * Cửa sổ này tự xử lý việc tải nội dung từ máy chủ FTP.
 */
public class FileViewer extends JFrame {
    private JTextArea textArea;
    /**
     * Tạo một cửa sổ xem tệp mới.
     * Cửa sổ sẽ hiển thị thông báo "Đang tải..." và sau đó tải nội dung tệp
     * trên một luồng nền để không làm treo giao diện người dùng.
     *
     * @param filename      Tên của tệp, được hiển thị trên tiêu đề cửa sổ.
     * @param controlWriter Trình ghi cho kết nối điều khiển FTP.
     * @param controlReader Trình đọc cho kết nối điều khiển FTP.
     */
    public FileViewer(String filename, PrintWriter controlWriter, BufferedReader controlReader) {
        setTitle("Xem tệp: " + filename);
        setSize(700, 500);
        setLocationRelativeTo(null); // Hiển thị cửa sổ ở giữa màn hình
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Chỉ đóng cửa sổ này, không thoát ứng dụng

        // Sử dụng JTextArea để hiển thị nội dung, cho phép xuống dòng và bao bọc từ
        textArea = new JTextArea("Đang tải nội dung, vui lòng chờ...");
        textArea.setEditable(false); // Không cho phép chỉnh sửa
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14)); // Font chữ đơn cách để dễ đọc code/log
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        // Thêm JTextArea vào một JScrollPane để có thể cuộn
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        // Hiển thị cửa sổ trước khi bắt đầu tải để người dùng thấy thông báo
        setVisible(true);

        // Sử dụng SwingWorker để tải tệp trên một luồng nền, tránh làm treo giao diện
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                // Thực hiện việc tải tệp ở đây
                return Retr.getFileContent(controlReader, controlWriter, filename);
            }

            @Override
            protected void done() {
                try {
                    String fileContent = get(); // Lấy kết quả từ doInBackground()
                    textArea.setText(fileContent);
                    textArea.setCaretPosition(0); // Cuộn lên đầu văn bản
                } catch (Exception e) {
                    // Nếu có lỗi (ví dụ: IOException), hiển thị thông báo và đóng cửa sổ
                    JOptionPane.showMessageDialog(FileViewer.this, "Không thể đọc nội dung tệp: " + e.getMessage(), "Lỗi Đọc Tệp", JOptionPane.ERROR_MESSAGE);
                    dispose(); // Đóng cửa sổ FileViewer
                }
            }
        };

        worker.execute(); // Bắt đầu thực thi SwingWorker
    }
}