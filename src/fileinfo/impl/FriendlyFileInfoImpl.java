package fileinfo.impl;

import fileinfo.FileInfo;
import fileinfo.FriendlyFileInfo;

public class FriendlyFileInfoImpl implements FriendlyFileInfo {
    
    private final FileInfo INFO;
    private static final String FORMAT = "%1$-30s | %2$-15s | %3$-10s | %4$-10s | %5$-10s | %6$-13s";

    public FriendlyFileInfoImpl(FileInfo INFO) {
        this.INFO = INFO;
    }

    @Override
    public String toString() {
        FileInfo i = getFileInfo();
        return String.format(FORMAT, i.getName(), getFriendlyLastModified(), getFriendlySize(), i.getType(), i.getExtension(), (i.isDirectory() ? "" : "not ") + "directory");
    }
    
    @Override
    public FileInfo getFileInfo() {
        return INFO;
    }
    
    @Override
    public String getFriendlySize() {
        return TypeConverter.getFriendlySize(getFileInfo().getSize());
    }
    
    @Override
    public String getFriendlyLastModified() {
        return TypeConverter.getFriendlyDate(getFileInfo().getLastModified());
    }

    @Override
    public Object getValueAt(int index) {
        switch (index) {
            case 0:
                return getFileInfo().getName();
            case 1:
                return getFriendlyLastModified();
            case 2:
                return getFriendlySize();
            default:
                return "";
        }
    }

    @Override
    public String[] getColumnNames() {
        return new String[] {"Név", "Módosítás dátuma", "Méret"};
    }
    
}