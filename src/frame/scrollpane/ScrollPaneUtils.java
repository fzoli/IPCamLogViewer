package frame.scrollpane;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

public class ScrollPaneUtils {
    
    public static JScrollPane createDefaultScrollPane(Component component) {
        return createDefaultScrollPane(component, -1, -1);
    }
    
    public static JScrollPane createDefaultScrollPane(Component component, int width, int height) {
        JScrollPane p = new JScrollPane(component);
        p.setBorder(BorderFactory.createEmptyBorder(-1, -1, -1, -1));
        if (width >= 0 || height >= 0) p.setPreferredSize(new Dimension(width, height));
        return p;
    }
    
}