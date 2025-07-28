package methods;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Quit extends WindowAdapter {
    private final PrintWriter controlWriter;
    private final BufferedReader controlReader;
    private final Socket controlSocket;

    /**
     * Khởi tạo một trình xử lý sự kiện thoát, giữ các tham chiếu đến tài nguyên kết nối.
     * @param controlWriter PrintWriter của kết nối điều khiển.
     * @param controlReader BufferedReader của kết nối điều khiển.
     * @param controlSocket Socket của kết nối điều khiển.
     */
    public Quit(PrintWriter controlWriter, BufferedReader controlReader, Socket controlSocket) {
        this.controlWriter = controlWriter;
        this.controlReader = controlReader;
        this.controlSocket = controlSocket;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        doQuit();
    }

    /**
     * Thực hiện hành động thoát: gửi lệnh QUIT đến server, đóng các tài nguyên
     * và kết thúc ứng dụng.
     * Phương thức này có thể được gọi từ nhiều nơi (nút thoát, menu, đóng cửa sổ).
     */
    public void doQuit() {
        try {
            if (controlWriter != null) {
                controlWriter.println("QUIT"); // Gửi lệnh thoát một cách lịch sự
                controlWriter.close();
            }
            if (controlReader != null) controlReader.close();
            if (controlSocket != null && !controlSocket.isClosed()) controlSocket.close();
            System.out.println("Đã đóng kết nối FTP và thoát ứng dụng.");
        } catch (IOException ex) {
            System.err.println("Lỗi khi đóng kết nối FTP: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            System.exit(0); // Đảm bảo ứng dụng luôn thoát
        }
    }
}
