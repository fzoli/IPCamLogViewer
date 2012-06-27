package fileinfo;

import java.util.Date;

public interface FileInfo {
    
    boolean isDirectory();
    
    String getExtension();
    
    String getName();

    Date getLastModified();
    
    long getSize();
    
    String getType();
    
}