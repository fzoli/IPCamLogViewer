package fileinfo.impl.lighttpd;

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

public class LighttpdFileInfoParser implements FileInfoParser {
    
    private static final String[] LIGHTTPD_VERSIONS = {"1.4.28"};
    
    private static final String TYPE_DIRECTORY = "Directory";
    private static final String OCTET_STREAM = "application/octet-stream";
    private static final Pattern P = Pattern.compile("(<tr><td class=\"n\"><a href=\")(.*?)(\">.*?<td class=\"m\">)(.*?)(</td><td class=\"s\">)(.*?)(<.*?<td class=\"t\">)(.*?)</td></tr>");
    private static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss", Locale.ENGLISH); //2011-Mar-06 19:46:42

    private String src;
    private final List<String> SUPPORTED_SERVERS;
    
    public LighttpdFileInfoParser() {
        this(null);
    }
    
    public LighttpdFileInfoParser(String src) {
        this.src = src;
        this.SUPPORTED_SERVERS = createSupportedServerList();
    }
    
    @Override
    public void setSrc(String src) {
        this.src = src;
    }
    
    @Override
    public List<String> getSupportedServers() {
        return SUPPORTED_SERVERS;
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
    
    private static List<String> createSupportedServerList() {
        return ServerUtil.createSupportedServerList("lighttpd", LIGHTTPD_VERSIONS);
    }
    
    private static Date parseDate(String date) {
        try {
            return FORMATTER.parse(date);
        }
        catch (ParseException ex) {
            return null;
        }
    }
    
    private static long parseSize(String size) {
        try {
            int lastIndex = size.length() - 1;
            char prefix = size.charAt(lastIndex);
            if (prefix != 'K' && prefix != 'M' && prefix != 'G') {
                lastIndex = size.length();
                prefix = ' ';
            }
            size = size.substring(0, lastIndex);
            double s = Double.valueOf(size);
            switch (prefix) {
                case ' ':
                    return (long) s;
                case 'K':
                    return (long)(s * 1024.0); 
                case 'M':
                    return (long)(s * 1048576.0);
                case 'G':
                    return (long)(s * 1073741824.0);
                default:
                    return -1;
            }
        }
        catch (Exception ex) {
            return -1;
        }
    }
    
    private List<FileInfo> getFileInfoList(String extension, boolean isDir, boolean chckDir) {
        if (src == null) return new ArrayList<FileInfo>();
        Matcher m = P.matcher(src);
        List<FileInfo> ls = new ArrayList<FileInfo>();
        while (m.find()) {
            String fileUrl = m.group(2);
            if (!fileUrl.equals("../")) { //filter Parent Directory
                String fileType = m.group(8);
                boolean isFileDir = TYPE_DIRECTORY.equals(fileType);
                if (isFileDir == isDir || !chckDir) {
                    fileUrl = isFileDir ? fileUrl.substring(0, fileUrl.length() - 1) : fileUrl; //in order to get readable file name, if file is directory remove / from the end of url
                    //create file info. if file is directory file size will be null AND if file type is unknown (octet stream) file size will be null too
                    FileInfo info = new FileInfoImpl(fileUrl, parseDate(m.group(4)), isFileDir ? -1 : parseSize(m.group(6)), OCTET_STREAM.equals(fileType) ? null : fileType, isFileDir);
                    //add to list if extension equals to file's extension OR extension is null
                    if (equal(extension, info.getExtension())) ls.add(info);
                }
            }
        }
        return ls;
    }
    
    private static boolean equal(Object x, Object y) {
        return ServerUtil.equal(x, y);
    }

}