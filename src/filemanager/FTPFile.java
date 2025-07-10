package filemanager;

import java.util.Date;

/**
 * Lớp đại diện cho một tệp hoặc thư mục trên máy chủ FTP.
 * Chứa các thông tin đã được phân tích cú pháp như tên, kích thước, ngày sửa đổi, v.v.
 */
public class FTPFile {
    private final String name;
    private final long size;
    private final boolean isDirectory;
    private final Date lastModified;

    public FTPFile(String name, long size, boolean isDirectory, Date lastModified) {
        this.name = name;
        this.size = size;
        this.isDirectory = isDirectory;
        this.lastModified = lastModified;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Trả về kích thước dưới dạng chuỗi có thể đọc được (B, KB, MB, GB).
     * Đối với thư mục, trả về một chuỗi rỗng.
     */
    public String getFormattedSize() {
        if (isDirectory) {
            return "";
        }
        if (size < 1024) return size + " B";
        int exp = (int) (Math.log(size) / Math.log(1024));
        return String.format("%.1f %sB", size / Math.pow(1024, exp), "KMGTPE".charAt(exp - 1));
    }
}