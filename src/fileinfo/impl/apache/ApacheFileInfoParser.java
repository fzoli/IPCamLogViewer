package fileinfo.impl.apache;

import fileinfo.FileInfo;
import fileinfo.FileInfoParser;
import fileinfo.impl.FileInfoImpl;
import fileinfo.impl.ServerUtil;
import fileinfo.impl.lighttpd.LighttpdFileInfoParser;
import http.HttpExecutor;
import java.io.IOException;
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
public class ApacheFileInfoParser implements FileInfoParser {

    private static final String[] APACHE_VERSIONS = {"2.4.10"};
    
    private static final String TYPE_PARENT_DIR = "PARENTDIR", TYPE_DIR = "DIR", TYPE_IMG = "IMG";
    
    private static final Pattern P = Pattern.compile("<tr><td valign=\"top\"><img src=\"/icons/.*\\.gif\" alt=\"\\[(.*)\\]\"></td><td><a href=\"(.*)\">(.*)</a></td><td align=\"right\">(.*)</td><td align=\"right\">(.*)</td><td>&nbsp;</td></tr>");
    private static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH); // 2014-08-12 21:28
    
    private String src;
    private final List<String> SUPPORTED_SERVERS;
    
    public ApacheFileInfoParser() {
        this(null);
    }
    
    public ApacheFileInfoParser(String src) {
        this.src = src;
        this.SUPPORTED_SERVERS = createSupportedServerList();
    }
    
    private static List<String> createSupportedServerList() {
        return ServerUtil.createSupportedServerList("Apache", APACHE_VERSIONS);
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
    
    private List<FileInfo> getFileInfoList(String extension, boolean isDir, boolean chckDir) {
        if (src == null) return new ArrayList<FileInfo>();
        Matcher m = P.matcher(src);
        List<FileInfo> ls = new ArrayList<FileInfo>();
        while (m.find()) {
            String type = m.group(1);
            String url = m.group(2);
            String date = m.group(4);
            String size = m.group(5);
            boolean isFileDir = TYPE_DIR.equals(type) || TYPE_PARENT_DIR.equals(type);
            if (isFileDir == isDir || !chckDir) {
                url = isFileDir ? url.substring(0, url.length() - 1) : url; //in order to get readable file name, if file is directory remove / from the end of url
                FileInfo info = new FileInfoImpl(url, parseDate(date), isFileDir ? -1 : LighttpdFileInfoParser.parseSize(size), isFileDir ? null : type, isFileDir);
                //add to list if extension equals to file's extension OR extension is null
                if (ServerUtil.equal(extension, info.getExtension())) ls.add(info);
            }
        }
        return ls;
    }
    
    private static Date parseDate(String date) {
        try {
            return FORMATTER.parse(date.trim());
        }
        catch (ParseException ex) {
            return null;
        }
    }
    
    public static void main(String[] args) throws IOException {
        String s = new HttpExecutor("http://nb-server/private/motion/cam0/2009/08/29/").setRequestType(HttpExecutor.RequestType.GET).getResponse();
        for (FileInfo f : new ApacheFileInfoParser(s).getFileInfoList(false)) {
            System.out.println(f);
        }
    }
    
}
