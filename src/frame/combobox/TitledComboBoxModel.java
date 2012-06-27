package frame.combobox;

import java.util.Vector;
import javax.swing.DefaultComboBoxModel;

public class TitledComboBoxModel<E> extends DefaultComboBoxModel<E> {

    private boolean selected = false;
    
    public TitledComboBoxModel() {
    }

    public TitledComboBoxModel(Vector<E> v) {
        super(v);
    }

    public TitledComboBoxModel(E[] items) {
        super(items);
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public Object getSelectedItem() {
        return super.getSelectedItem();
    }
    
    public boolean isSelected() {
        return selected;
    }
    
}