package fileinfo;

import java.util.List;

public interface FileInfoParser {
    
    int UNAUTHORIZED = 401;
    int NOT_FOUND = 404;
    int OK = 200;
    
    void setSrc(String src);
    
    List<FileInfo> getFileInfoList();
    
    List<FileInfo> getFileInfoList(String extension);
    
    List<FileInfo> getFileInfoList(boolean isDirectory);
    
    List<String> getSupportedServers();
    
}