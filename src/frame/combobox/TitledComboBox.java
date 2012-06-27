package frame.combobox;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.*;

public class TitledComboBox<E> extends JComboBox<E> {

    private String defTitle, title;
    private ListCellRenderer<? super E> oldRenderer;
    
    private class TitledComboBoxRenderer implements ListCellRenderer<E> {

        @Override
        public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
            Component comp = oldRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            JLabel lb = (JLabel) comp;
            if (!isPopupVisible() && !isSelected()) lb.setText(title);
            return comp;
        }
    }
    
    public TitledComboBox(String title) {
        super();
        init(title, true);
    }

    public TitledComboBox(String title, TitledComboBoxModel<E> aModel) {
        super(aModel);
        init(title, false);
    }

    public TitledComboBox(String title, E[] items) {
        super(items);
        init(title, true);
    }

    public TitledComboBox(String title, Vector<E> items) {
        super(items);
        init(title, true);
    }
    
    public void resetTitle() {
        setTitle(defTitle);
    }
    
    public void setTitle(String text) {
        title = text;
        setSelected(false);
    }
    
    private void init(String title, boolean switchModel) {
        setOpaque(false);
        if (switchModel) switchModel();
        this.defTitle = title;
        this.oldRenderer = getRenderer();
        setRenderer(new TitledComboBoxRenderer());
        resetTitle();
        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setSelected(true);
            }
            
        });
        
    }

    @Override
    public void setEditable(boolean aFlag) {
        if (aFlag) System.out.println("TitledComboBox is not editable!");
        super.setEditable(false);
    }
    
    public boolean isSelected() {
        return ((TitledComboBoxModel)getModel()).isSelected();
    }
    
    public void setSelected(boolean b) {
        ((TitledComboBoxModel)getModel()).setSelected(b);
    }

    @Override
    public void setModel(ComboBoxModel<E> aModel) {
        super.setModel(aModel);
        switchModel();
    }
    
    private void switchModel() {
        if (getModel() instanceof TitledComboBoxModel) return;
        Vector v = new Vector();
        for (int i = 0; ; i++) {
            E obj = getModel().getElementAt(i);
            if (obj == null) break;
            v.add(obj);
        }
        setModel(new TitledComboBoxModel<E>(v));
        if (v.size() == 0) resetTitle();
    }
    
}