package MainWindow;

import methods.Delete;
import methods.Mkdir;
import methods.Rename;
import methods.Retrieve;

import MainWindow.filemanager.FTPFile;

import javax.swing.JMenu;
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

        JMenu sortMenu = new JMenu("Sắp xếp");
        JMenuItem sortByName = new JMenuItem("Theo Tên");
        JMenuItem sortBySize = new JMenuItem("Theo Kích thước");
        JMenuItem sortByDate = new JMenuItem("Theo Ngày sửa đổi (mới nhất)");

        sortByName.addActionListener(e -> fileList.sortFileList(FileList.SortCriteria.NAME));
        sortBySize.addActionListener(e -> fileList.sortFileList(FileList.SortCriteria.SIZE));
        sortByDate.addActionListener(e -> fileList.sortFileList(FileList.SortCriteria.DATE));

        sortMenu.add(sortByName);
        sortMenu.add(sortBySize);
        sortMenu.add(sortByDate);

        JMenuItem refreshItem = new JMenuItem("Tải lại");
        refreshItem.addActionListener(e -> fileList.refreshFileList());

        add(sortMenu);
        add(refreshItem);

        // Thêm dải phân cách và menu sắp xếp
        addSeparator();

        JMenu newMenu = new JMenu("Tạo mới");
        JMenuItem newFolderItem = new JMenuItem("Thư mục mới");

        newFolderItem.addActionListener(e -> Mkdir.initiateCreateDirectory(fileList));

        newMenu.add(newFolderItem);
        add(newMenu);

        addSeparator();

        JMenuItem downloadItem = new JMenuItem("Tải xuống");
        JMenuItem deleteItem = new JMenuItem("Xóa");
        JMenuItem renameItem = new JMenuItem("Đổi tên");

        // Lấy số lượng hàng đang được chọn để quyết định trạng thái của các mục menu
        int[] selectedRows = fileList.getFileTable().getSelectedRows();
        int selectionCount = selectedRows.length;

        // Gán hành động cho từng mục menu, gọi đến các phương thức xử lý tương ứng
        downloadItem.addActionListener(e -> Retrieve.handleDownloadAction(fileList));
        deleteItem.addActionListener(e -> Delete.handleDeleteAction(fileList));
        renameItem.addActionListener(e -> Rename.handleRenameAction(fileList));

        // Bật/tắt các mục menu dựa trên lựa chọn
        downloadItem.setEnabled(selectionCount > 0);
        deleteItem.setEnabled(selectionCount > 0);
        renameItem.setEnabled(selectionCount == 1);

        // Thêm các mục vào menu
        add(downloadItem);
        add(deleteItem);
        add(renameItem);

        addSeparator();

        JMenuItem propertiesItem = new JMenuItem("Lấy thông tin");
        propertiesItem.addActionListener(e -> {
            if (selectionCount == 1) {
                FTPFile selectedFile = fileList.getCurrentFiles().get(selectedRows[0]);
                String currentPath = fileList.getCurrentPath();
                // Mở dialog thuộc tính
                new Properties(fileList, selectedFile, currentPath);
            }
        });
        propertiesItem.setEnabled(selectionCount == 1);

        add(propertiesItem);
    }
}
