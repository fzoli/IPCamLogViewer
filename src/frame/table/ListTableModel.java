package frame.table;

import java.util.List;
import javax.swing.table.AbstractTableModel;

public class ListTableModel extends AbstractTableModel {
    
    private String[] colNames;
    private int count = 0;
    private List<ListTableModelHelper> helpers;
    
    public void updateList(List<ListTableModelHelper> list) {
        helpers = list;
        count = helpers.size();
        if (count > 0) {
            colNames = helpers.get(0).getColumnNames();
        }
        fireTableDataChanged();
    }

    @Override
    public String getColumnName(int column) {
        if (colNames == null) return "";
        return colNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (count == 0) return Object.class;
        return getValueAt(0, columnIndex).getClass();
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    @Override
    public int getRowCount() {
        return count;
    }
    
    @Override
    public int getColumnCount() {
        if (colNames == null) return 0;
        return colNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ListTableModelHelper p = helpers.get(rowIndex);
        return p.getValueAt(columnIndex);
    }
    
}