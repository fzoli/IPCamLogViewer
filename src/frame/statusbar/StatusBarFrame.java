package frame.statusbar;

import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JPanel;
import resources.ResourceUtils;

public class StatusBarFrame extends JFrame {
    
    private JPanel content;
    private StatusBar statusBar;

    public StatusBarFrame() {
        initStatusBar();
        setIconImage(ResourceUtils.getImage("icon.png"));
    }
    
    private void initStatusBar() {
        StatusBarUtils.StatusBarComponents sbc = StatusBarUtils.initStatusBarPanel(this);
        statusBar = sbc.getStatusBar();
        content = sbc.getContent();
    }
    
    public StatusBar getStatusBar() {
        return statusBar;
    }
    
    @Override
    public Container getContentPane() {
        if (content != null) return content;
        else return super.getContentPane();
    }
    
}