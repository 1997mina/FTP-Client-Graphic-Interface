package MainWindow.filemanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Phân tích cú pháp đầu ra từ lệnh LIST của FTP.
 * Định dạng có thể khác nhau giữa các máy chủ, lớp này xử lý một định dạng phổ biến (giống ls -l).
 */
public class FTPFileParser {

    // Mẫu regex để khớp với định dạng đầu ra của lệnh LIST trên nhiều hệ thống Unix.
    // Ví dụ: drwxr-xr-x   1 user     group           0 Jan 21 15:33 public_html
    //        -rw-r--r--   1 user     group        1024 Feb 15 2023  index.html
    private static final Pattern PATTERN = Pattern.compile(
            "([d-])([rwxt-]{9})\\s+\\d+\\s+\\S+\\s+\\S+\\s+(\\d+)\\s+([A-Za-z]{3}\\s+\\d{1,2}\\s+(?:\\d{1,2}:\\d{2}|\\d{4}))\\s+(.*)");

    // Định dạng ngày tháng có thể xuất hiện trong kết quả LIST
    private static final SimpleDateFormat DATE_FORMAT_WITH_TIME;
    private static final SimpleDateFormat DATE_FORMAT_WITH_YEAR;

    static {
        // Giả định rằng thời gian từ máy chủ FTP là giờ UTC.
        // Điều này cho phép chuyển đổi chính xác sang múi giờ địa phương của người dùng khi hiển thị.
        DATE_FORMAT_WITH_TIME = new SimpleDateFormat("MMM dd HH:mm", Locale.ENGLISH);
        DATE_FORMAT_WITH_TIME.setTimeZone(TimeZone.getTimeZone("UTC"));
        DATE_FORMAT_WITH_YEAR = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH);
        DATE_FORMAT_WITH_YEAR.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static List<FTPFile> parse(String listing) {
        List<FTPFile> files = new ArrayList<>();
        String[] lines = listing.split("\\r?\\n");

        for (String line : lines) {
            Matcher matcher = PATTERN.matcher(line);
            if (matcher.matches()) {
                try {
                    boolean isDirectory = "d".equals(matcher.group(1));
                    long size = Long.parseLong(matcher.group(3));
                    String dateStr = matcher.group(4);
                    String name = matcher.group(5).trim();

                    Date lastModified;
                    // Định dạng LIST của FTP thường bỏ qua năm đối với các tệp gần đây.
                    // Chúng ta cần xử lý trường hợp này một cách chính xác.
                    if (dateStr.contains(":")) {
                        // Định dạng có thời gian (ví dụ: "Jan 21 15:33")
                        // Giả định năm hiện tại, nhưng nếu ngày đó trong tương lai,
                        // thì đó phải là năm ngoái. Tất cả các phép tính được thực hiện trong UTC.
                        lastModified = DATE_FORMAT_WITH_TIME.parse(dateStr);

                        // Sử dụng Lịch UTC để tránh các vấn đề về múi giờ khi đặt năm.
                        Calendar parsedCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        parsedCal.setTime(lastModified);

                        // Lấy năm hiện tại cũng trong UTC để so sánh nhất quán.
                        Calendar currentCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        parsedCal.set(Calendar.YEAR, currentCal.get(Calendar.YEAR));

                        // Nếu ngày đã phân tích (với năm hiện tại) ở trong tương lai,
                        // thì nó phải là từ năm trước.
                        if (parsedCal.after(currentCal)) {
                            parsedCal.add(Calendar.YEAR, -1);
                        }
                        lastModified = parsedCal.getTime();
                    } else {
                        // Định dạng có năm (ví dụ: "Feb 15 2023")
                        lastModified = DATE_FORMAT_WITH_YEAR.parse(dateStr);
                    }

                    files.add(new FTPFile(name, size, isDirectory, lastModified));
                } catch (ParseException | NumberFormatException e) {
                    System.err.println("Không thể phân tích dòng: " + line + " - Lỗi: " + e.getMessage());
                }
            }
        }
        return files;
    }
}