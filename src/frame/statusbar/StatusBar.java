package frame.statusbar;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import resources.ResourceUtils;

public class StatusBar extends JPanel {

    private JPanel rightPanel;
    private JLabel lbIcon, lbMessage;
    private Icon idleIcon, errorIcon;
    private ArrayList<Icon> busyIcons = new ArrayList<Icon>();
    
    private Timer animation = new Timer(30, new ActionListener() {

        private int index = 0;
        
        @Override
        public void actionPerformed(ActionEvent e) {
            int index = (this.index++) % busyIcons.size();
            lbIcon.setIcon(busyIcons.get(index));
        }
        
    });
    
    StatusBar() {
        setBorder();
        initComponents();
    }
    
    public void reset() {
        setMessage(" ");
    }
    
    public void setMessage(String message) {
        setMessage(message, false);
    }
    
    public void setError(String message) {
        setMessage(message, true);
    }
    
    public void setProgress(String message) {
        setMessage(message);
        animation.start();
    }
    
    public JPanel getRightPanel() {
        return rightPanel;
    }
    
    private void setMessage(String message, boolean isError) {
        lbMessage.setText(message);
        animation.stop();
        setIcon(isError);
    }
    
    private void setIcon(boolean isError) {
        Icon i = isError ? errorIcon : idleIcon;
        lbIcon.setIcon(i);
    }
    
    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        c.gridx = 1;
        c.weightx = 0;
        c.anchor = c.LINE_START;
        c.fill = c.BOTH;
        c.insets = new Insets(2, 6, 2, 0);
        idleIcon = createIdleIcon();
        lbIcon = new JLabel(idleIcon);
        add(lbIcon, c);
        
        c.gridx = 2;
        add(new JSeparator(JSeparator.VERTICAL), c);
        
        c.gridx = 3;
        c.weightx = 1;
        lbMessage = new JLabel(" ");
        add(lbMessage, c);
        
        c.gridx = 4;
        c.weightx = 0;
        c.insets = new Insets(0, 0, 0, 5);
        c.anchor = c.LINE_END;
        rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout());
        add(rightPanel, c);
        
        initBusyIcons();
        initErrorIcon();
    }
    
    private Icon createIdleIcon() {
        Image icon = getImage("idle-icon.png");
        return new ImageIcon(icon);
    }

    private void initErrorIcon() {
        Image icon = getImage("error-icon.png");
        errorIcon = new ImageIcon(icon);
    }
    
    private void initBusyIcons() {
        for (int i = 0; i <= 14; i++) {
            Image icon = getImage("busy-icon" + i + ".png");
            busyIcons.add(new ImageIcon(icon));
        }
    }
    
    private Image getImage(String filename) {
        return ResourceUtils.getBusyImage(filename);
    }
    
    private void setBorder() {
        Border b1 = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        Border b2 = BorderFactory.createEmptyBorder(0, -2, -2, -2);
        setBorder(BorderFactory.createCompoundBorder(b2, b1));
    }
    
}