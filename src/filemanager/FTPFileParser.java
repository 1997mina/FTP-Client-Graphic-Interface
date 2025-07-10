package filemanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
    private static final SimpleDateFormat DATE_FORMAT_WITH_TIME = new SimpleDateFormat("MMM dd HH:mm", Locale.ENGLISH);
    private static final SimpleDateFormat DATE_FORMAT_WITH_YEAR = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH);

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
                    if (dateStr.contains(":")) {
                        lastModified = DATE_FORMAT_WITH_TIME.parse(dateStr);
                    } else {
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