package MainWindow;

import methods.Delete;
import methods.Rename;
import methods.Retrieve;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Lớp này tạo một menu ngữ cảnh (popup menu) để hiển thị khi người dùng
 * nhấp chuột phải vào một file trong danh sách.
 */
@SuppressWarnings("unused")
public class PopUpMenu extends JPopupMenu {
    /**
     * Tạo một menu ngữ cảnh mới được liên kết với một FileList cụ thể.
     * @param fileList Cửa sổ FileList nơi menu này sẽ được hiển thị.
     */
    public PopUpMenu(FileList fileList) {
        // Tạo các mục menu
        JMenuItem downloadItem = new JMenuItem("Tải xuống");
        JMenuItem deleteItem = new JMenuItem("Xóa");
        JMenuItem renameItem = new JMenuItem("Đổi tên");

        // Gán hành động cho từng mục menu, gọi đến các phương thức xử lý tương ứng
        downloadItem.addActionListener(e -> Retrieve.handleDownloadAction(fileList));
        deleteItem.addActionListener(e -> Delete.handleDeleteAction(fileList));
        renameItem.addActionListener(e -> Rename.handleRenameAction(fileList));

        // Thêm các mục vào menu
        add(downloadItem);
        add(deleteItem);
        add(renameItem);
    }
}
