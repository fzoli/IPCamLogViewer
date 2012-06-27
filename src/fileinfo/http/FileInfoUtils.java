package fileinfo.http;

import fileinfo.FileInfo;
import fileinfo.FileInfoParser;
import fileinfo.FriendlyFileInfo;
import fileinfo.impl.FriendlyFileInfoImpl;
import http.HttpExecutor;
import java.util.ArrayList;
import java.util.List;

public class FileInfoUtils {
    
    public static  List<FriendlyFileInfo> getFileInfoList(HttpExecutor executor, FileInfoParser parser, String url) {
        executor.setUrl(url);
        String resp = executor.getResponse();
        parser.setSrc(resp);
        List<FileInfo> files = parser.getFileInfoList();
        return createFriendlyList(files);
    }
    
    private static List<FriendlyFileInfo> createFriendlyList(List<FileInfo> l) {
        List<FriendlyFileInfo> ffiles = new ArrayList<FriendlyFileInfo>();
        for (FileInfo fi : l) {
            ffiles.add(new FriendlyFileInfoImpl(fi));
        }
        return ffiles;
    }
    
}