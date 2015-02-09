package net.mineguild.Launcher;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class CustomizationPanel extends JPanel {
  public CustomizationPanel() {
    
    JLabel lblAddMods = new JLabel("Add Mods - Overview - Deactivate/Activate Mods - ResourcePacks? ");
    add(lblAddMods);
  }
  
  
  
  public static class CustomizationPanelTableModel implements TableModel{

    @Override
    public int getRowCount() {
      return 0;
    }

    @Override
    public int getColumnCount() {
      return 2; //Name, ModInfo(If existing) 
    }

    @Override
    public String getColumnName(int columnIndex) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
      // TODO Auto-generated method stub
      
    }
    
  }

}
