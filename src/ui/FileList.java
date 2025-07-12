package ui;

import methods.Cwd;
import methods.Quit;
import methods.List;

import filemanager.FTPFile;
import filemanager.FTPFileParser;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.net.Socket;
import java.text.SimpleDateFormat;

public class FileList extends JFrame {

    private final BufferedReader controlReader;
    private final PrintWriter controlWriter;
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private Toolbar toolbar; // Thêm một tham chiếu đến toolbar
    private java.util.List<FTPFile> currentFiles;

    /**
     * Tạo một cửa sổ mới để hiển thị danh sách các tệp từ máy chủ FTP dưới dạng bảng.
     * @param controlSocket Socket điều khiển kết nối.
     * @param controlReader Luồng để đọc phản hồi từ server.
     * @param controlWriter Luồng để gửi lệnh đến server.
     */
    public FileList(Socket controlSocket, BufferedReader controlReader, PrintWriter controlWriter) {
        this.controlReader = controlReader;
        this.controlWriter = controlWriter;

        // Thiết lập layout chính cho JFrame để có thể đặt toolbar ở trên
        setLayout(new BorderLayout());

        setTitle("Danh sách tệp trên Server");
        setSize(800, 600);
        // Thay đổi hành vi đóng mặc định và thêm listener để dọn dẹp tài nguyên
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Gán một thực thể của lớp Quit làm trình xử lý sự kiện đóng cửa sổ
        addWindowListener(new Quit(controlWriter, controlReader, controlSocket));
        initializeTable();

        // Thêm thanh công cụ vào phía trên (Bắc) của cửa sổ
        this.toolbar = new Toolbar(this);
        add(this.toolbar, BorderLayout.NORTH);

        refreshFileList();

        setVisible(true);
    }

    /**
     * Các phương thức getter để cho phép Toolbar truy cập các thành phần của FileList.
     */
    public JTable getFileTable() { return fileTable; }
    public java.util.List<FTPFile> getCurrentFiles() { return currentFiles; }
    public PrintWriter getControlWriter() { return controlWriter; }
    public BufferedReader getControlReader() { return controlReader;
    }

    private void initializeTable() {
        // Định nghĩa các cột cho bảng
        String[] columnNames = {"", "Tên", "Kích thước", "Ngày sửa đổi"};

        // Tạo một TableModel không cho phép chỉnh sửa trực tiếp
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
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
        fileTable.setRowHeight(25); // Tăng chiều cao hàng để icon hiển thị đẹp hơn

        // Thêm trình nghe sự kiện nhấp đúp chuột
        fileTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2 && fileTable.getSelectedRow() != -1) {
                    handleDoubleClick();
                }
            }
        });

        // Thiết lập chiều rộng cho cột icon
        // In đậm header
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

        add(new JScrollPane(fileTable), BorderLayout.CENTER);
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

        FTPFile selectedFile = currentFiles.get(selectedRow);

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
        }
    }

    /**
     * Lấy danh sách tệp từ máy chủ và điền vào bảng.
     */
    public void refreshFileList() {
        try {
            String fileListString = List.getFileList(controlReader, controlWriter);
            this.currentFiles = FTPFileParser.parse(fileListString);

            // Xóa dữ liệu cũ trong bảng
            tableModel.setRowCount(0);

            // Lấy icon hệ thống cho thư mục và file
            Icon folderIcon = getSystemIcon(true);
            Icon fileIcon = getSystemIcon(false);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            // Thêm từng file/thư mục vào bảng
            for (FTPFile ftpFile : this.currentFiles) {
                Object[] rowData = {
                        ftpFile.isDirectory() ? folderIcon : fileIcon,
                        ftpFile.getName(),
                        ftpFile.getFormattedSize(),
                        sdf.format(ftpFile.getLastModified())
                };
                tableModel.addRow(rowData);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy danh sách tệp: " + e.getMessage(), "Lỗi FTP", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        // Cập nhật trạng thái các nút sau khi làm mới danh sách
        toolbar.updateButtonStates();
    }

    /**
     * Lấy icon hệ thống mặc định cho file hoặc thư mục.
     * @param isDirectory true để lấy icon thư mục, false để lấy icon file.
     * @return Icon tương ứng.
     */
    private Icon getSystemIcon(boolean isDirectory) {
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
