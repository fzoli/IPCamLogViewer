package main;

import fileinfo.impl.apache.ApacheFileInfoParser;
import fileinfo.impl.lighttpd.LighttpdFileInfoParser;
import fileinfo.impl.uhttpd.UhttpdFileInfoParser;
import frame.DateChooser;
import frame.UserInfoAsker;
import frame.statusbar.StatusBar;
import java.awt.GraphicsEnvironment;
import javax.swing.UIManager;
import org.apache.http.HttpStatus;

public class Main {

    public static DateChooser dateChooser;
    
    public static final String ROOT_URL_S1 = "https://farcsal.hu/private/motion/";
    public static final String LOCAL_ROOT_URL_S1 = "http://192.168.10.254:8008/motion/";

    public static final String ROOT_URL_S2 = "https://farcsal.hu:8081/private/motion/";
    public static final String LOCAL_ROOT_URL_S2 = "http://192.168.10.5/private/motion/";
    
    public static final String LIVE_URL = "https://farcsal.hu/ipcam/cam0";
    public static final String LOCAL_LIVE_URL = "http://192.168.10.254:9000";
    
    private static boolean local = false;
    
    private static void setLookAndFeel() {
        if (!GraphicsEnvironment.isHeadless()) {
            try {
                // Linuxra GTK LAF beállítása
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            }
            catch (Exception ex) {
                // Ha nem Linuxon fut a program, rendszer LAF beállítása
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
                catch (Exception e) {
                    ;
                }
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        setLookAndFeel();
        final Server LS1 = Server.create("Router LAN", LOCAL_ROOT_URL_S1, new UhttpdFileInfoParser(), true);
        final Server RS1 = Server.create("Router WAN", ROOT_URL_S1, new LighttpdFileInfoParser(), false);
        final Server LS2 = Server.create("Debian LAN", LOCAL_ROOT_URL_S2, new ApacheFileInfoParser(), true);
        final Server RS2 = Server.create("Debian WAN", ROOT_URL_S2, new ApacheFileInfoParser(), false);
        final UserInfoAsker asker = new UserInfoAsker(RS1.executor, RS2.executor);
        final StatusBar sb = asker.getStatusBar();
        sb.setProgress("Névtelen bejelentkezés kísérlet...");
        new Thread(new Runnable() {

            @Override
            public void run() {
                LS1.executor.getResponse();
                if(LS1.executor.getStatus() != HttpStatus.SC_OK) {
                    sb.reset();
                }
                else {
                    local = true;
                    asker.dispose();
                }
            }
            
        }).start();
        if(!asker.waitValidInfo() && !local) return;
        if (!asker.isDisposed()) asker.dispose();
        if (local) dateChooser = new DateChooser(new Server[] {LS1, LS2}, false, LOCAL_LIVE_URL, asker);
        else dateChooser = new DateChooser(new Server[] {RS1, RS2}, true, LIVE_URL, asker);
    }
    
}