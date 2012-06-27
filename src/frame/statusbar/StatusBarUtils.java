package frame.statusbar;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;

public class StatusBarUtils {
    
    static final class StatusBarComponents {
        
        private StatusBar sb;
        private JPanel p;

        public StatusBarComponents(StatusBar sb, JPanel p) {
            this.sb = sb;
            this.p = p;
        }
        
        public StatusBar getStatusBar() {
            return sb;
        }
        
        public JPanel getContent() {
            return p;
        }
        
    }
    
    public static void setMinimumFrameWidth(JToolBar tb, JFrame frame) {
        Component lastComponent = tb.getComponentAtIndex(tb.getComponentCount() - 1);
        int px = lastComponent.getLocation().x;
        int sw = lastComponent.getSize().width;
        frame.setMinimumSize(new Dimension(px + sw + 10, 0));
    }
    
    static StatusBarUtils.StatusBarComponents initStatusBarPanel(Window window) {
        window.setLayout(new BorderLayout());
        StatusBar statusBar = new StatusBar();
        window.add(statusBar, BorderLayout.PAGE_END);
        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        window.add(content, BorderLayout.CENTER);
        return new StatusBarUtils.StatusBarComponents(statusBar, content);
    }
    
}