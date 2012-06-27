package frame.toolbar;

import java.awt.Component;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import resources.ResourceUtils;

public class ToolbarUtils {
    
    public static Border createToolbarBorder() {
        Border b1 = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        Border b2 = BorderFactory.createEmptyBorder(-2, -2, 0, -2);
        return BorderFactory.createCompoundBorder(b2, b1);
    }
    
    public static JToolBar createDefaultToolbar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        tb.setBorder(ToolbarUtils.createToolbarBorder());
        return tb;
    }
    
    public static JToggleButton createToggleToolbarButton(String iconFilename, String tooltipText) {
        return (JToggleButton) createToolbarButton(iconFilename, tooltipText, true);
    }
    
    public static JButton createToolbarButton(String iconFilename, String tooltipText) {
        return (JButton) createToolbarButton(iconFilename, tooltipText, false);
    }
    
    private static AbstractButton createToolbarButton(String iconFilename, String tooltipText, boolean toggle) {
        AbstractButton c;
        Icon icon = ResourceUtils.getToolbarIcon(iconFilename);
        c = toggle ? new JToggleButton(icon) : new JButton(icon);
        if (tooltipText != null) c.setToolTipText(tooltipText);
        c.setFocusable(false);
        c.setOpaque(false);
        return c;
    }
    
}