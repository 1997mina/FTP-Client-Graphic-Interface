package methods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Pasv {
    /**
     * Enters passive mode and establishes a data connection.
     * @param controlReader The reader for the control connection.
     * @param controlWriter The writer for the control connection.
     * @return A Socket for the data connection.
     * @throws IOException if there's an error with the FTP communication or connection.
     */

    public static Socket openDataConnection(BufferedReader controlReader, PrintWriter controlWriter) throws IOException {
        // 1. Send PASV command
        controlWriter.println("PASV");
        String pasvResponse = controlReader.readLine();
        if (pasvResponse == null || !pasvResponse.startsWith("227")) {
            throw new IOException("Không thể vào chế độ passive: " + pasvResponse);
        }

        // 2. Parse IP and Port for the data connection
        int openParen = pasvResponse.indexOf('(');
        int closeParen = pasvResponse.indexOf(')');
        if (openParen == -1 || closeParen == -1) {
            throw new IOException("Định dạng phản hồi PASV không hợp lệ: " + pasvResponse);
        }
        String data = pasvResponse.substring(openParen + 1, closeParen);
        String[] parts = data.split(",");
        if (parts.length < 6) {
            throw new IOException("Định dạng phản hồi PASV không hợp lệ, không đủ phần: " + pasvResponse);
        }
        String dataIp = parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];
        int p1 = Integer.parseInt(parts[4]);
        int p2 = Integer.parseInt(parts[5]);
        int dataPort = p1 * 256 + p2;

        // 3. Open and return the data connection socket
        return new Socket(dataIp, dataPort);
    }
}
