package main;

import fileinfo.impl.lighttpd.LighttpdFileInfoParser;
import fileinfo.impl.uhttpd.UhttpdFileInfoParser;
import frame.DateChooser;
import frame.UserInfoAsker;
import frame.statusbar.StatusBar;
import http.HttpExecutor;
import javax.swing.UIManager;
import org.apache.http.HttpStatus;

public class Main {

    public static DateChooser dateChooser;
    
    public static final String ROOT_URL = "https://fzoli.dyndns.org/private/motion/";
    public static final String LOCAL_ROOT_URL = "http://192.168.10.254:8008/motion/";

    private static boolean local = false;
    
    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {}
    }
    
    public static void main(String[] args) throws Exception {
        setLookAndFeel();
        final HttpExecutor le = new HttpExecutor(LOCAL_ROOT_URL);
        final HttpExecutor e = new HttpExecutor(ROOT_URL, false);
        final UserInfoAsker asker = new UserInfoAsker(e);
        final StatusBar sb = asker.getStatusBar();
        sb.setProgress("Névtelen bejelentkezés kísérlet...");
        new Thread(new Runnable() {

            @Override
            public void run() {
                le.getResponse();
                if(le.getStatus() != HttpStatus.SC_OK) {
                    sb.reset();
                }
                else {
                    local = true;
                    asker.dispose();
                }
            }
            
        }).start();
        if(!asker.waitValidInfo() && !local) return;
        asker.dispose();
        if (local) dateChooser = new DateChooser(LOCAL_ROOT_URL, e, new UhttpdFileInfoParser());
        else dateChooser = new DateChooser(ROOT_URL, e, new LighttpdFileInfoParser());
    }
    
}