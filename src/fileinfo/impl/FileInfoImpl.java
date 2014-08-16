package fileinfo.impl;

import fileinfo.FileInfo;
import java.util.Date;

public class FileInfoImpl implements FileInfo {
    
    private final long SIZE;
    private final Date LAST_MODIFIED;
    private final String NAME, TYPE, EXTENSION;
    private final boolean IS_DIRECTORY;

    public FileInfoImpl(String name, Date lastModified, long size, String type, boolean directory) {
        this.NAME = name;
        this.LAST_MODIFIED = lastModified;
        this.SIZE = size;
        this.TYPE = type;
        this.IS_DIRECTORY = directory;
        this.EXTENSION = createExtension();
    }

    private String createExtension() {
        if (isDirectory()) return null;
        try {
            int i = getName().lastIndexOf(".");
            if (i != -1) return getName().substring(i + 1);
        }
        catch (Exception ex) {}
        return null;
    }
    
    @Override
    public boolean isDirectory() {
        return IS_DIRECTORY;
    }
    
    @Override
    public String getExtension() {
        return EXTENSION;
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Date getLastModified() {
        return LAST_MODIFIED;
    }

    @Override
    public long getSize() {
        return SIZE;
    }
    
    @Override
    public String getType() {
        return isDirectory() ? null : TYPE;
    }

    @Override
    public String toString() {
        return getName() + " - " + getType() + " - " + getExtension() + " - " + getLastModified() + " - " + getSize();
    }
    
}
