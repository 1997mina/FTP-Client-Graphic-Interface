package ui;

import methods.Retrieve;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;

/**
 * Một cửa sổ để hiển thị nội dung của một tệp ảnh.
 * Cửa sổ này tự xử lý việc tải nội dung từ máy chủ FTP.
 */
public class ImageViewer extends JFrame {
    private final JLabel imageLabel;

    /**
     * Tạo một cửa sổ xem ảnh mới.
     * Cửa sổ sẽ hiển thị thông báo "Đang tải..." và sau đó tải nội dung ảnh
     * trên một luồng nền để không làm treo giao diện người dùng.
     *
     * @param filename      Tên của tệp, được hiển thị trên tiêu đề cửa sổ.
     * @param controlWriter Trình ghi cho kết nối điều khiển FTP.
     * @param controlReader Trình đọc cho kết nối điều khiển FTP.
     */
    public ImageViewer(String filename, PrintWriter controlWriter, BufferedReader controlReader) {
        setTitle("Xem ảnh: " + filename);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        imageLabel = new JLabel("Đang tải ảnh, vui lòng chờ...", SwingConstants.CENTER);
        imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);

        SwingWorker<byte[], Void> worker = new SwingWorker<>() {
            @Override
            protected byte[] doInBackground() throws Exception {
                // Tải dữ liệu ảnh dưới dạng byte bằng phương thức mới
                return Retrieve.getFileBytes(controlReader, controlWriter, filename);
            }

            @Override
            protected void done() {
                try {
                    byte[] imageData = get();
                    ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
                    BufferedImage image = ImageIO.read(bais);
                    if (image != null) {
                        imageLabel.setText(null);
                        imageLabel.setIcon(new ImageIcon(image));
                        pack(); // Điều chỉnh kích thước cửa sổ cho vừa với ảnh
                        setLocationRelativeTo(null);
                    } else {
                        imageLabel.setText("Không thể hiển thị ảnh. Định dạng không được hỗ trợ hoặc tệp bị lỗi.");
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ImageViewer.this, "Không thể tải ảnh: " + e.getMessage(), "Lỗi Tải Ảnh", JOptionPane.ERROR_MESSAGE);
                    dispose();
                }
            }
        };
        worker.execute();
    }
}
