package fileinfo.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class TypeConverter {

    private static final DecimalFormat DEC = new DecimalFormat("0.00");
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss");
    private static final int GB_PREF = 1073741824;
    private static final int MB_PREF = 1048576;
    private static final int KB_PREF = 1024;
    
    public static String getFriendlySize(double size) {
        if (size < 0) return "-";
        String sz, p;
        double s;
        if (size / GB_PREF >= 1) {
            s = size/GB_PREF;
            p = "G";
        }
        else if (size / MB_PREF >= 1) {
            s = size / MB_PREF;
            p = "M";
        }
        else if (size / KB_PREF >= 1) {
            s = size / KB_PREF;
            p = "K";
        }
        else {
            s = size;
            p = "";
        }
        sz = DEC.format(s);
        return sz.concat(" " + p + "B");
    }
    
    public static String getFriendlyDate(Date date) {
        return FORMATTER.format(date);
    }
    
}