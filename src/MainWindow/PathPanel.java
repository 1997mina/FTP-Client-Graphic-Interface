package MainWindow;

import methods.Cwd;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Một JPanel tùy chỉnh để hiển thị đường dẫn hiện tại dưới dạng "breadcrumb"
 * có thể nhấp được.
 */
public class PathPanel extends JPanel {

    private final FileList fileList;

    /**
     * Tạo một PathPanel mới.
     * @param fileList Tham chiếu đến FileList chính để thực hiện các hành động điều hướng.
     */
    public PathPanel(FileList fileList) {
        super(new FlowLayout(FlowLayout.LEFT, 5, 5));
        this.fileList = fileList;
        setBorder(BorderFactory.createEtchedBorder());
    }

    /**
     * Cập nhật thanh breadcrumb để hiển thị đường dẫn hiện tại.
     * Mỗi thành phần của đường dẫn sẽ là một JLabel có thể nhấp được.
     * @param path Đường dẫn hiện tại, ví dụ: "/public/images"
     */
    public void updatePath(String path) {
        removeAll();

        // Tạo một label cho thư mục gốc
        add(createPathLabel("/", "/"));

        if (path == null || path.equals("/") || path.isEmpty()) {
            revalidate();
            repaint();
            return;
        }

        String[] components = path.startsWith("/") ? path.substring(1).split("/") : path.split("/");
        StringBuilder pathBuilder = new StringBuilder();

        for (int i = 0; i < components.length; i++) {
            if (components[i].isEmpty()) continue;

            add(new JLabel(">"));
            pathBuilder.append("/").append(components[i]);
            String targetPath = pathBuilder.toString();

            // Thành phần cuối cùng không cần là link, chỉ là text
            add(i == components.length - 1 ? new JLabel(components[i]) : createPathLabel(components[i], targetPath));
        }

        revalidate();
        repaint();
    }

    private JLabel createPathLabel(String text, String targetPath) {
        JLabel label = new JLabel("<html><u>" + text + "</u></html>");
        label.setForeground(Color.BLUE.darker());
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Cwd.changeDirectoryAndRefresh(fileList, targetPath);
            }
        });
        return label;
    }
}