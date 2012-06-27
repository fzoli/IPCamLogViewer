package fileinfo.impl.uhttpd;

import fileinfo.FileInfo;
import fileinfo.FileInfoParser;
import fileinfo.impl.FileInfoImpl;
import fileinfo.impl.ServerUtil;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author zoli
 */
public class UhttpdFileInfoParser implements FileInfoParser {

    private String src;
    private static final String[] UHTTPD_VERSIONS = {"2.2"};
    private static final String TYPE_DIRECTORY = "directory";
    private static final String OCTET_STREAM = "application/octet-stream";
    private final List<String> SUPPORTED_SERVERS;
    private static final DateFormat FORMATTER = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH); //Sun, 06 Mar 2011 12:23:17 GMT
    private static final Pattern P = Pattern.compile("<li><strong><a href='(.*?)'>(.*?)</a>.*?<small>modified: (.*?)<br />.*?(..*?) - (.*?)<br />.*?</li>");

    public UhttpdFileInfoParser() {
        this(null);
    }

    public UhttpdFileInfoParser(String src) {
        this.src = src;
        this.SUPPORTED_SERVERS = createSupportedServerList();
    }
    
    @Override
    public void setSrc(String src) {
        this.src = src;
    }

    private static Date parseDate(String date) {
        try {
            return FORMATTER.parse(date);
        }
        catch (ParseException ex) {
            return null;
        }
    }
    
    private boolean isDirecotry(String type) {
        return TYPE_DIRECTORY.equals(type);
    }
    
    private static long parseSize(String size) {
        double d = Double.parseDouble(size.substring(0, size.indexOf(" kbyte")));
        return (long)(d * 1024.0);
    }
    
    public List<FileInfo> getFileInfoList(String extension, boolean isDir, boolean chckDir) {
        Matcher m = P.matcher(src);
        List<FileInfo> ls = new ArrayList<FileInfo>();
        while (m.find()) {
            String fileUrl = m.group(1);
            fileUrl = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            if (!fileUrl.equals("..")) { //filter Parent Directory
                String fileType = m.group(4);
                boolean isFileDir = TYPE_DIRECTORY.equals(fileType);
                if (isFileDir == isDir || !chckDir) {
                    FileInfo info = new FileInfoImpl(fileUrl, parseDate(m.group(3)), isDirecotry(fileType) ? -1 : parseSize(m.group(5)), OCTET_STREAM.equals(fileType) ? null : fileType, isFileDir);
                    if (ServerUtil.equal(extension, info.getExtension())) ls.add(info);
                }
            }
        }
        return ls;
    }

    @Override
    public List<String> getSupportedServers() {
        return SUPPORTED_SERVERS;
    }

    private static List<String> createSupportedServerList() {
        return ServerUtil.createSupportedServerList("uhttpd", UHTTPD_VERSIONS);
    }

    @Override
    public List<FileInfo> getFileInfoList() {
        return getFileInfoList(null, false, false);
    }
    
    @Override
    public List<FileInfo> getFileInfoList(String extension) {
        return getFileInfoList(extension, false, true);
    }
    
    @Override
    public List<FileInfo> getFileInfoList(boolean isDirectory) {
        return getFileInfoList(null, isDirectory, true);
    }
    
}