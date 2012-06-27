package fileinfo;

import frame.table.ListTableModelHelper;

public interface FriendlyFileInfo extends ListTableModelHelper {
    
    FileInfo getFileInfo();
    
    String getFriendlySize();
    
    String getFriendlyLastModified();
    
}