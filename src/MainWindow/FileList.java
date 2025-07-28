package MainWindow;

import methods.*;
import MainWindow.Applications.*;
import MainWindow.Applications.Media.MediaPlayer;
import MainWindow.filemanager.*;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.event.CellEditorListener;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Comparator;
import java.net.Socket;
import java.text.SimpleDateFormat;

public class FileList extends JFrame {

    private final BufferedReader controlReader;
    private final PrintWriter controlWriter;
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private Toolbar toolbar; // Thêm một tham chiếu đến toolbar
    private JPanel centerPanel; // Panel chứa bảng và thanh tìm kiếm
    private PathPanel pathPanel; // Sử dụng lớp PathPanel chuyên dụng
    private java.util.List<FTPFile> currentFiles;
    private java.util.List<FTPFile> displayedFiles; // Files currently shown in the table
    private String currentPath;
    private SearchBar searchBar;
    // Biến trạng thái để kiểm soát việc đổi tên trực tiếp
    private Quit quitHandler;
    public enum EditMode {
        NONE, RENAME, CREATE
    }
    public EditMode editMode = EditMode.NONE;
    public int editingRow = -1;

    public enum SortCriteria {
        NAME, SIZE, DATE
    }

    /**
     * Tạo một cửa sổ mới để hiển thị danh sách các tệp từ máy chủ FTP dưới dạng bảng.
     * @param controlSocket Socket điều khiển kết nối.
     * @param controlReader Luồng để đọc phản hồi từ server.
     * @param controlWriter Luồng để gửi lệnh đến server.
     */
    @SuppressWarnings("unused")
    public FileList(Socket controlSocket, BufferedReader controlReader, PrintWriter controlWriter) {
        this.controlReader = controlReader;
        this.controlWriter = controlWriter;
        this.displayedFiles = new java.util.ArrayList<>();

        // Thiết lập layout chính cho JFrame để có thể đặt toolbar ở trên
        setLayout(new BorderLayout());

        setTitle("Trình quản lý tệp trên Server");
        setSize(800, 600);
        // Thay đổi hành vi đóng mặc định và thêm listener để dọn dẹp tài nguyên
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Khởi tạo và gán trình xử lý thoát
        this.quitHandler = new Quit(controlWriter, controlReader, controlSocket);
        addWindowListener(this.quitHandler);

        // Khởi tạo bảng và lấy JScrollPane chứa nó
        JScrollPane scrollPane = initializeTable();

        // Khởi tạo panel đường dẫn chuyên dụng
        pathPanel = new PathPanel(this);

        // Thêm thanh công cụ
        this.toolbar = new Toolbar(this);

        // Thêm listener cho bảng sau khi toolbar đã được tạo để cập nhật trạng thái nút
        fileTable.getSelectionModel().addListSelectionListener(e -> this.toolbar.updateButtonStates());
        // Tạo một panel để chứa cả toolbar và trường đường dẫn
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(this.toolbar, BorderLayout.NORTH);
        topPanel.add(pathPanel, BorderLayout.SOUTH);

        // Khởi tạo thanh tìm kiếm chuyên dụng
        this.searchBar = new SearchBar(this);

        // --- Sắp xếp các panel chính ---
        centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        // --- Thiết lập phím tắt (Ctrl+F) ---
        JRootPane rootPane = this.getRootPane();
        KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK);

        // Gán phím tắt với hành động
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlF, "TOGGLE_SEARCH_BAR");
        rootPane.getActionMap().put("TOGGLE_SEARCH_BAR", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSearchBar();
            }
        });

        refreshFileList();

        setVisible(true);
    }

    /**
     * Hiển thị hoặc ẩn thanh tìm kiếm.
     * Được gọi bởi nút tìm kiếm trên thanh công cụ.
     */
    public void toggleSearchBar() {
        if (searchBar.isShowing()) {
            // Nếu đang hiển thị, hãy ẩn nó đi
            centerPanel.remove(searchBar);
            // Đặt lại tìm kiếm để hiển thị tất cả các tệp khi thanh tìm kiếm bị ẩn.
            searchFiles("");
        } else {
            // Nếu đang ẩn, hãy hiển thị nó ở trên cùng
            centerPanel.add(searchBar, BorderLayout.NORTH);
            searchBar.requestFocusInSearchField();
        }
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    /**
     * Lọc danh sách tệp dựa trên một truy vấn tìm kiếm và cập nhật bảng.
     * @param query Từ khóa tìm kiếm. Nếu rỗng, tất cả các tệp sẽ được hiển thị.
     */
    public void searchFiles(String query) {
        // Nếu không có tệp gốc, không làm gì cả
        if (currentFiles == null) return;

        String lowerCaseQuery = query.toLowerCase().trim();

        if (lowerCaseQuery.isEmpty()) {
            // Nếu truy vấn rỗng, hiển thị tất cả các tệp
            displayedFiles = new java.util.ArrayList<>(currentFiles);
        } else {
            // Lọc danh sách tệp
            displayedFiles = currentFiles.stream()
                    .filter(file -> file.getName().toLowerCase().contains(lowerCaseQuery))
                    .collect(java.util.stream.Collectors.toList());
        }
        updateTableDisplay();
    }

    /**
     * Các phương thức getter để cho phép Toolbar truy cập các thành phần của FileList.
     */
    public JTable getFileTable() { return fileTable; }
    public java.util.List<FTPFile> getCurrentFiles() { return displayedFiles; } // Trả về danh sách đã lọc
    public PrintWriter getControlWriter() { return controlWriter; }
    public BufferedReader getControlReader() { return controlReader; }
    public String getCurrentPath() { return currentPath; }
    public Toolbar getToolbar() { return toolbar; }
    public Quit getQuitHandler() { return quitHandler; }
	public DefaultTableModel getTableModel() { return tableModel; }

    private JScrollPane initializeTable() {
        // Định nghĩa các cột cho bảng
        String[] columnNames = {"", "Tên", "Kích thước", "Ngày sửa đổi"};

        // Tạo một TableModel không cho phép chỉnh sửa trực tiếp
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Chỉ cho phép chỉnh sửa cột "Tên" (index 1) nếu đang trong trạng thái chỉnh sửa
                return (editMode == EditMode.RENAME || editMode == EditMode.CREATE) && row == editingRow && column == 1;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                // Đảm bảo rằng việc đặt giá trị chỉ xảy ra khi đang trong chế độ đổi tên
                if (editMode != EditMode.NONE && row == editingRow && column == 1) {
                    String newName = ((String) aValue).trim();

                    if (editMode == EditMode.RENAME) {
                        FTPFile fileToRename = displayedFiles.get(row);
                        String oldName = fileToRename.getName();

                        if (!newName.isEmpty() && !newName.equals(oldName)) {
                            Rename.performRename(FileList.this, oldName, newName);
                        } else {
                            // Nếu tên không thay đổi, chỉ cần làm mới danh sách để thoát chế độ chỉnh sửa
                            refreshFileList();
                        }
                    } else if (editMode == EditMode.CREATE) {
                        if (!newName.isEmpty()) {
                            Mkdir.performCreateDirectory(FileList.this, newName);
                        } else {
                            // Nếu người dùng không nhập tên, chỉ cần làm mới danh sách để loại bỏ hàng tạm thời
                            refreshFileList();
                        }
                    }
                }
            }

            // Quan trọng: Cho phép JTable hiển thị các đối tượng Icon
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Icon.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };
        
        fileTable = new JTable(tableModel);
        fileTable.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Tăng cỡ chữ
        fileTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // Cho phép chọn nhiều hàng
        fileTable.setRowHeight(25); // Tăng chiều cao hàng để icon hiển thị đẹp hơn

        // Thêm listener để xử lý việc kết thúc hoặc hủy bỏ chỉnh sửa tên tệp.
        // Điều này đảm bảo trạng thái `isRenaming` được đặt lại và các nút trên toolbar
        // được kích hoạt lại một cách chính xác.
        fileTable.getDefaultEditor(Object.class).addCellEditorListener(new CellEditorListener() {
            /**
             * Phương thức này được gọi khi việc chỉnh sửa kết thúc hoặc bị hủy.
             * Nó đặt lại trạng thái đổi tên và cập nhật các nút trên thanh công cụ.
             */
            private void resetRenamingState() {
                editMode = EditMode.NONE;
                editingRow = -1;
                toolbar.updateButtonStates();
            }

            @Override
            public void editingStopped(javax.swing.event.ChangeEvent e) {
                // Logic xử lý đã nằm trong setValueAt, nơi sẽ gọi refreshFileList().
                // refreshFileList() sẽ tự động đặt lại trạng thái.
            }

            @Override
            public void editingCanceled(javax.swing.event.ChangeEvent e) {
                if (editMode == EditMode.CREATE) {
                    tableModel.removeRow(editingRow); // Xóa hàng tạm thời
                }
                resetRenamingState();
            }
        });

        // Tạo một MouseAdapter duy nhất để xử lý các sự kiện chuột trên cả JTable và vùng trống.
        MouseAdapter mouseListener = new MouseAdapter() {
            private void showPopupMenu(MouseEvent e) {
                // Chuyển đổi điểm nhấp chuột sang hệ tọa độ của JTable để xác định hàng.
                // Điều này đảm bảo nó hoạt động chính xác ngay cả khi sự kiện đến từ JScrollPane.
                Point point = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), fileTable);
                int row = fileTable.rowAtPoint(point);

                // Nếu nhấp vào một hàng hợp lệ và hàng đó chưa được chọn,
                // hãy xóa lựa chọn trước đó và chỉ chọn hàng được nhấp.
                // Điều này giữ nguyên lựa chọn nhiều mục nếu người dùng nhấp chuột phải vào một mục đã được chọn.
                if (row != -1) {
                    if (!fileTable.isRowSelected(row)) {
                        fileTable.setRowSelectionInterval(row, row);
                    }
                } else {
                    // Nếu nhấp vào vùng trống (không phải trên hàng nào), hãy xóa lựa chọn.
                    fileTable.clearSelection();
                }

                // Luôn hiển thị menu bật lên. Các mục của nó sẽ được bật/tắt dựa trên lựa chọn hiện tại.
                PopUpMenu menu = new PopUpMenu(FileList.this);
                menu.show(e.getComponent(), e.getX(), e.getY());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // Xử lý nhấp đúp để mở tệp/thư mục.
                if (e.getClickCount() == 2 && fileTable.getSelectedRow() != -1) {
                    handleDoubleClick();
                }
                // Hiển thị menu bật lên khi có trình kích hoạt (dành cho Mac/Linux).
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Hiển thị menu bật lên khi có trình kích hoạt (dành cho Windows).
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }
        };
        // Thêm trình nghe sự kiện chuột vào bảng
        fileTable.addMouseListener(mouseListener);
        // Xử lý header cho danh sách file
        JTableHeader header = fileTable.getTableHeader();
        header.setFont(header.getFont().deriveFont(Font.BOLD, 16)); // Tăng cỡ chữ header

        // Thiết lập chiều rộng cho cột icon
        TableColumn iconColumn = fileTable.getColumnModel().getColumn(0);
        iconColumn.setPreferredWidth(30);
        iconColumn.setMaxWidth(30);

        // Thiết lập chiều rộng cho các cột khác
        fileTable.getColumnModel().getColumn(1).setPreferredWidth(300); // Tên
        fileTable.getColumnModel().getColumn(2).setPreferredWidth(50); // Kích thước
        fileTable.getColumnModel().getColumn(3).setPreferredWidth(250); // Ngày

        // Thêm trình nghe vào viewport của JScrollPane để bắt các cú nhấp chuột vào vùng trống.
        JScrollPane scrollPane = new JScrollPane(fileTable);
        scrollPane.getViewport().addMouseListener(mouseListener);
        return scrollPane;
    }

    /**

     * Xử lý sự kiện nhấp đúp chuột vào một mục trong bảng.
     * Nếu mục là một thư mục, nó sẽ cố gắng thay đổi thư mục làm việc hiện tại trên server.
     */
    private void handleDoubleClick() {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow == -1) {
            return; // Không có hàng nào được chọn
        }

        FTPFile selectedFile = displayedFiles.get(selectedRow);

        if (selectedFile.isDirectory()) {
            try {
                if (Cwd.changeDirectory(controlWriter, controlReader, selectedFile.getName())) {
                    // Nếu thay đổi thư mục thành công, làm mới danh sách tệp
                    refreshFileList();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Không thể truy cập thư mục '" + selectedFile.getName() + "'.",
                            "Lỗi Truy Cập", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Lỗi mạng khi đổi thư mục: " + e.getMessage(), "Lỗi Mạng", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else {
            // Nếu là tệp, kiểm tra xem đó có phải là tệp ảnh không.
            String filename = selectedFile.getName();
            if (isImageFile(filename)) {
                // Mở bằng trình xem ảnh mới
                new ImageViewer(filename, controlWriter, controlReader);
            } else if (isVideoFile(filename)) {
                // Tải và mở bằng trình phát video mặc định của hệ thống
                MediaPlayer.openVideo(this, selectedFile);
            } else if (isAudioFile(filename)) {
                // Tải và mở bằng trình phát audio mặc định của hệ thống
                MediaPlayer.openAudio(this, selectedFile);
            } else {
                // Mở bằng trình xem văn bản mặc định
                new FileViewer(filename, controlWriter, controlReader);
            }
        }
    }

    /**
     * Kiểm tra xem một tệp có phải là tệp ảnh dựa trên phần mở rộng hay không.
     * @param filename Tên tệp.
     * @return true nếu là tệp ảnh, ngược lại là false.
     */
    private boolean isImageFile(String filename) {
        String lowerCaseName = filename.toLowerCase();
        return lowerCaseName.endsWith(".png") || lowerCaseName.endsWith(".jpg") ||
               lowerCaseName.endsWith(".jpeg") || lowerCaseName.endsWith(".gif") ||
               lowerCaseName.endsWith(".bmp");
    }

    /**
     * Kiểm tra xem một tệp có phải là tệp video dựa trên phần mở rộng hay không.
     * @param filename Tên tệp.
     * @return true nếu là tệp video, ngược lại là false.
     */
    private boolean isVideoFile(String filename) {
        String lowerCaseName = filename.toLowerCase();
        return lowerCaseName.endsWith(".mp4") || lowerCaseName.endsWith(".avi") ||
               lowerCaseName.endsWith(".mkv") || lowerCaseName.endsWith(".mov") ||
               lowerCaseName.endsWith(".wmv");
    }

    /**
     * Kiểm tra xem một tệp có phải là tệp âm thanh dựa trên phần mở rộng hay không.
     * @param filename Tên tệp.
     * @return true nếu là tệp âm thanh, ngược lại là false.
     */
    private boolean isAudioFile(String filename) {
        String lowerCaseName = filename.toLowerCase();
        return lowerCaseName.endsWith(".mp3") || lowerCaseName.endsWith(".wav") ||
               lowerCaseName.endsWith(".flac") || lowerCaseName.endsWith(".aac") ||
               lowerCaseName.endsWith(".ogg");
    }

    /**
     * Sắp xếp danh sách tệp hiện tại theo tiêu chí đã cho và cập nhật bảng.
     * @param criteria Tiêu chí để sắp xếp (Tên, Kích thước, Ngày).
     */
    public void sortFileList(SortCriteria criteria) {
        if (displayedFiles == null || displayedFiles.isEmpty()) {
            return;
        }

        // Sắp xếp thư mục trước, sau đó đến tệp, rồi mới theo tiêu chí phụ.
        Comparator<FTPFile> comparator = Comparator.comparing(FTPFile::isDirectory).reversed();

        switch (criteria) {
            case NAME:
                comparator = comparator.thenComparing(FTPFile::getName, String.CASE_INSENSITIVE_ORDER);
                break;
            case SIZE:
                comparator = comparator.thenComparingLong(FTPFile::getSize).reversed();
                break;
            case DATE:
                comparator = comparator.thenComparing(FTPFile::getLastModified).reversed();
                break;
        }

        displayedFiles.sort(comparator);
        updateTableDisplay();
    }
    /**
     * Lấy danh sách tệp từ máy chủ và điền vào bảng.
     */
    public void refreshFileList() {
        // Đặt lại bất kỳ trạng thái chỉnh sửa nào trước khi làm mới
        this.editMode = EditMode.NONE;
        this.editingRow = -1;

        // Nếu thanh tìm kiếm đang hiển thị, hãy ẩn nó đi và xóa văn bản
        if (this.searchBar != null && this.searchBar.isShowing()) {
            centerPanel.remove(this.searchBar);
            this.searchBar.clearSearchText(); // Xóa văn bản để đảm bảo giao diện nhất quán
            searchFiles(""); // Reset the search
        }

        try {
            // Lấy đường dẫn hiện tại trước khi làm mới danh sách
            this.currentPath = Pwd.getCurrentDirectory(controlWriter, controlReader);
            // Cập nhật thanh breadcrumbs thông qua lớp PathPanel
            pathPanel.updatePath(this.currentPath);

            String fileListString = methods.List.getFileList(controlReader, controlWriter);
            this.currentFiles = FTPFileParser.parse(fileListString);

            // Sau khi làm mới, hiển thị tất cả các tệp
            searchFiles("");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy danh sách tệp: " + e.getMessage(), "Lỗi FTP", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Xóa và vẽ lại bảng với dữ liệu từ `currentFiles`.
     */
    private void updateTableDisplay() {
        // Xóa dữ liệu cũ trong bảng
        tableModel.setRowCount(0);

        if (displayedFiles == null) return;

        // Lấy icon hệ thống cho thư mục và file
        Icon folderIcon = getSystemIcon(true);
        Icon fileIcon = getSystemIcon(false);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        // Thêm từng file/thư mục từ danh sách đã lọc vào bảng
        for (FTPFile ftpFile : this.displayedFiles) {
            Object[] rowData = {
                    ftpFile.isDirectory() ? folderIcon : fileIcon,
                    ftpFile.getName(),
                    ftpFile.getFormattedSize(),
                    sdf.format(ftpFile.getLastModified())
            };
            tableModel.addRow(rowData);
        }

        toolbar.updateButtonStates();
    }

    /**
     * Lấy icon hệ thống mặc định cho file hoặc thư mục.
     * @param isDirectory true để lấy icon thư mục, false để lấy icon file.
     * @return Icon tương ứng.
     */
	public Icon getSystemIcon(boolean isDirectory) {
        File tempFile = null;
        try {
            tempFile = isDirectory ? File.createTempFile("tempdir", "") : File.createTempFile("tempfile", ".tmp");
            if (isDirectory) {
                tempFile.delete();
                tempFile.mkdir();
            }
            return FileSystemView.getFileSystemView().getSystemIcon(tempFile);
        } catch (IOException e) {
            // Trả về null nếu có lỗi, hoặc có thể trả về một icon mặc định từ resources
            return null;
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
