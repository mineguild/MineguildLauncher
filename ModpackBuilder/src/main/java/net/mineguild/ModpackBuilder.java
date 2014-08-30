package net.mineguild;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.io.FilenameUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

@SuppressWarnings("serial")
public class ModpackBuilder extends JFrame {

  public static File modpackDirectory;
  JButton selectFolderButton;
  JButton startButton;
  JTextField pathField;
  JTextField versionField;
  JPanel formPanel;
  public static ModpackBuilder instance;

  public ModpackBuilder() {
    instance = this;
    setTitle("Mineguild ModpackBuilder");
    versionField = new JTextField();
    new GhostText(versionField, "v1.0");
    selectFolderButton = new JButton("Select...");
    pathField = new JTextField();
    startButton = new JButton("Start!");
    pathField.setText(new File("").getAbsolutePath());
    FormLayout layout = new FormLayout("right:pref, 3dlu, pref, 3dlu, right:pref", // columns
        "p, 3dlu, p, 3dlu"); // rows
    PanelBuilder builder = new PanelBuilder(layout);
    CellConstraints cc = new CellConstraints();
    builder.addLabel("New Modpack", cc.xy(1, 1));
    builder.add(pathField, cc.xy(3, 1));
    builder.add(selectFolderButton, cc.xy(5, 1));
    builder.addLabel("Version", cc.xy(1, 3));
    builder.add(versionField, cc.xy(3, 3));
    formPanel = builder.getPanel();
    selectFolderButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (ModpackBuilder.getModpackDirectory(ModpackBuilder.instance)) {
          pathField.setText(modpackDirectory.getAbsolutePath());
          pack();
        }
      }
    });
    pathField.setInputVerifier(new InputVerifier() {

      @Override
      public boolean verify(JComponent input) {
        String text = ((JTextComponent) input).getText();
        File file = new File(text);
        if (file.exists() && file.isDirectory()) {
          if (new File(file, "mods").exists() && new File(file, "config").exists()) {
            return true;
          }
        }
        return false;
      }
    });
    add(formPanel, BorderLayout.CENTER);
    add(startButton, BorderLayout.SOUTH);
    pack();
    setLocationRelativeTo(null);
    pathField.requestFocus();
    getRootPane().setDefaultButton(selectFolderButton);
    startButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        String text = ModpackBuilder.instance.pathField.getText();
        File file = new File(text);
        if (file.exists() && file.isDirectory()) {
          if (new File(file, "mods").exists() && new File(file, "config").exists()) {
            if (versionField.getText().length() > 0) {
              modpackDirectory = new File(pathField.getText());
              createUpdatedPack(ModpackBuilder.instance);
            } else {
              JOptionPane.showMessageDialog(ModpackBuilder.instance,
                  "Version has to be non-empty!", "Error!", JOptionPane.ERROR_MESSAGE);
            }
          }
        } else {
          JOptionPane.showMessageDialog(ModpackBuilder.instance,
              "Invalid modpack-folder selected!", "Error!", JOptionPane.ERROR_MESSAGE);
        }
      }
    });

    setVisible(true);
    setDefaultCloseOperation(EXIT_ON_CLOSE);

  }

  public void createUpdatedPack(JFrame parent) {
    Modpack modPack = new Modpack(modpackDirectory);
    modPack.setVersion(ModpackBuilder.instance.versionField.getText());
    modPack.setReleaseTime(System.currentTimeMillis());
    WorkDialog dialog = new WorkDialog(parent);
    dialog.start(modPack);
    final JDialog showFilesDialog = new JDialog(parent);
    final ModpackTableModel mTableModel = new ModpackTableModel(modPack);
    final JTable table = new JTable(mTableModel);
    table.setLayout(new BorderLayout());
    JScrollPane tableView = new JScrollPane(table);
    JButton removeButton = new JButton("Remove selected entry/entries");
    JButton doneButton = new JButton("Done");
    doneButton.addActionListener(new ActionListener() {
      
      @Override
      public void actionPerformed(ActionEvent e) {
        showFilesDialog.dispose();
      }
    });
    removeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int[] rows = table.getSelectedRows();
        mTableModel.removeFiles(rows);
        table.clearSelection();
      }
    });
    
    showFilesDialog.add(tableView, BorderLayout.NORTH);
    showFilesDialog.add(removeButton, BorderLayout.CENTER);
    showFilesDialog.add(doneButton, BorderLayout.SOUTH);
    showFilesDialog.pack();
    showFilesDialog.setLocationRelativeTo(null);
    showFilesDialog.setModal(true);
    showFilesDialog.setVisible(true);
    System.out.println(modPack.toJson());
    System.exit(0);
  }



  public static void main(String[] args) throws Exception {
    try {
      UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
    } catch (Exception e) {
      e.printStackTrace();
    }
    new ModpackBuilder();
  }


  public static class ModpackTableModel extends AbstractTableModel {

    Modpack pack;

    public ModpackTableModel(Modpack pack) {
      this.pack = pack;
      this.pack.setModpackFiles(new TreeMap<>(pack.getModpackFiles()));
    }


    @Override
    public int getColumnCount() {
      // TODO Auto-generated method stub
      return 2;
    }


    @Override
    public String getColumnName(int arg0) {
      switch (arg0) {
        case 0:
          return "Filename";
        case 1:
          return "MD5-Hash";
        default:
          return "Unkown";
      }
    }

    @Override
    public int getRowCount() {
      return pack.getModpackFiles().size();
    }

    public void removeFile(int row) {
      removeFile((String) pack.getModpackFiles().keySet().toArray()[row]);
    }

    public void removeFiles(int[] rows) {
      int removedRows = 0;
      for (int row : rows) {
        System.out.printf("Removing row #%d\n", row);
        removeFile(row - removedRows);
      }
      fireTableRowsDeleted(rows[0], rows[rows.length - 1]);
    }

    public void removeFile(String name) {
      System.out.printf("Removing %s\n", name);
      pack.getModpackFiles().remove(name);
    }

    @Override
    public Object getValueAt(int row, int column) {
      if (column == 0) {
        return pack.getModpackFiles().keySet().toArray()[row];
      } else {
        return pack.getModpackFiles().values().toArray()[row];
      }
    }

    @Override
    public boolean isCellEditable(int t, int t2) {
      return false;
    }

  }

  public static boolean getModpackDirectory(Component parent) {
    JFileChooser fileChooser = new JFileChooser(new File("."));
    // FileNameExtensionFilter filter = new FileNameExtensionFilter("Modpack_Json", "json", "mmp");
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    // fileChooser.setFileFilter(filter);
    fileChooser.setDialogTitle("Select the directory of the modpack you want to update to.");
    int returnValue = fileChooser.showOpenDialog(parent);
    if (returnValue == JFileChooser.APPROVE_OPTION) {
      File selected = fileChooser.getSelectedFile();
      if ((new File(selected, "config")).exists() && new File(selected, "config").isDirectory()
          && (new File(selected, "mods")).exists() && (new File(selected, "mods")).isDirectory()) {
        modpackDirectory = selected;

        return true;
      } else {
        JOptionPane.showMessageDialog(null,
            "Invalid directory selected, please select a one containing mods and config folder.",
            "Invalid directory!", JOptionPane.ERROR_MESSAGE);
        getModpackDirectory(parent);
      }
    }
    return false;
  }


}
