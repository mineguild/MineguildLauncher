package net.mineguild.Builder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.JTextComponent;

import lombok.Getter;
import net.mineguild.Launcher.Constants;
import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.log.StdOutLogger;
import net.mineguild.Launcher.utils.DownloadUtils;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.json.BuilderSettings;
import net.mineguild.Launcher.utils.json.JsonFactory;
import net.mineguild.Launcher.utils.json.JsonWriter;
import net.mineguild.Launcher.utils.json.MCVersionIndex;
import net.mineguild.ModPack.ModPack;
import net.mineguild.ModPack.ModPackFile;
import net.mineguild.ModPack.Side;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.Lists;
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
  ModPack newestPack;
  @SuppressWarnings("rawtypes")
  JComboBox mcVersionBox;
  @SuppressWarnings("rawtypes")
  JComboBox forgeVersionBox;
  public static String forgeVersionIndex;
  public static ModpackBuilder instance;
  public static BuilderSettings settings;
  public static File forgeIndex = new File(OSUtils.getLocalDir(), "forgeIndex.html");
  public static File mcIndex = new File(OSUtils.getLocalDir(), "mcVersions.json");
  public static MCVersionIndex mcVersionIndex;


  public static void main(String[] args) throws Exception {
    DownloadUtils.ssl_hack();
    try {
      for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    Logger.addListener(new StdOutLogger());

    if (!forgeIndex.exists()) {
      FileUtils.copyURLToFile(new URL("http://files.minecraftforge.net/index.html"), forgeIndex);
    }
    forgeVersionIndex = FileUtils.readFileToString(forgeIndex);

    if (!mcIndex.exists()) {
      FileUtils.copyURLToFile(new URL(
          "https://s3.amazonaws.com/Minecraft.Download/versions/versions.json"), mcIndex);
    }
    mcVersionIndex = JsonFactory.loadVersionIndex(mcIndex);
    new ModpackBuilder();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public ModpackBuilder() throws MalformedURLException, IOException {
    instance = this;
    File newestFile = new File(OSUtils.getLocalDir(), "newest.json");
    FileUtils.copyURLToFile(new URL(Constants.MG_MMP + "modpack.json"), newestFile);
    newestPack = JsonFactory.loadModpack(newestFile);
    ModpackBuilder.settings =
        JsonFactory.loadBuilderSettings(new File(OSUtils.getLocalDir(), "builder.settings"));
    addSaveHook();
    setTitle("Mineguild ModpackBuilder");
    versionField = new JTextField();
    new GhostText(versionField, newestPack.getVersion());
    selectFolderButton = new JButton("Select...");
    pathField = new JTextField(ModpackBuilder.settings.getLastPath());
    JButton refreshForgeButton = new JButton("Refresh Forge-Versions");
    JButton refreshMCButton = new JButton("Refresh MC-Versions");
    refreshForgeButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          FileUtils
              .copyURLToFile(new URL("http://files.minecraftforge.net/index.html"), forgeIndex);
          forgeVersionIndex = FileUtils.readFileToString(forgeIndex);
          Object item = forgeVersionBox.getSelectedItem();
          forgeVersionBox.setModel(new JComboBox(getForgeForMC(
              (String) mcVersionBox.getSelectedItem()).toArray()).getModel());
          forgeVersionBox.setSelectedItem(item);
        } catch (Exception e1) {
          JOptionPane.showMessageDialog(rootPane, "Couldn't refresh!");
        }
      }
    });
    refreshMCButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          FileUtils.copyURLToFile(new URL(
              "https://s3.amazonaws.com/Minecraft.Download/versions/versions.json"), mcIndex);
          mcVersionIndex = JsonFactory.loadVersionIndex(mcIndex);
          Object item = mcVersionBox.getSelectedItem();
          mcVersionBox.setModel(new JComboBox(getMCVersions().toArray()).getModel());
          mcVersionBox.setSelectedItem(item);
        } catch (Exception e1) {
          JOptionPane.showMessageDialog(rootPane, "Couldn't refresh!");
        }
      }
    });
    mcVersionBox = new JComboBox(getMCVersions().toArray());
    mcVersionBox.setSelectedItem(newestPack.getMinecraftVersion());
    forgeVersionBox =
        new JComboBox(getForgeForMC((String) mcVersionBox.getSelectedItem()).toArray());
    forgeVersionBox.setSelectedItem(newestPack.getForgeVersion());
    startButton = new JButton("Start!");
    FormLayout layout = new FormLayout("right:pref, 3dlu, pref, 3dlu, right:pref", // columns
        "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu"); // rows
    PanelBuilder builder = new PanelBuilder(layout);
    CellConstraints cc = new CellConstraints();
    builder.addLabel("New Modpack", cc.xy(1, 1));
    builder.add(pathField, cc.xy(3, 1));
    builder.add(selectFolderButton, cc.xy(5, 1));
    builder.addLabel("Version", cc.xy(1, 3));
    builder.add(versionField, cc.xy(3, 3));
    builder.addLabel("MCVersion", cc.xy(1, 5));
    builder.add(mcVersionBox, cc.xy(3, 5));
    builder.addLabel("ForgeVersion", cc.xy(1, 7));
    builder.add(forgeVersionBox, cc.xy(3, 7));
    builder.add(refreshForgeButton, cc.xy(3, 9));
    builder.add(refreshMCButton, cc.xy(3, 11));
    formPanel = builder.getPanel();
    selectFolderButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (ModpackBuilder.getModpackDirectory(ModpackBuilder.instance)) {
          pathField.setText(modpackDirectory.getAbsolutePath());
          ModpackBuilder.settings.setLastPath(new File(pathField.getText()).getAbsolutePath());
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
    mcVersionBox.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("comboBoxChanged")) {
          try {
            forgeVersionBox.setModel(new JComboBox(getForgeForMC(
                (String) mcVersionBox.getSelectedItem()).toArray()).getModel());

          } catch (Exception e2) {
            JOptionPane.showMessageDialog(getRootPane(), "No forge versions for this mc version!");
          }
        }
      }
    });

    setVisible(true);
    setDefaultCloseOperation(EXIT_ON_CLOSE);

  }

  public void createUpdatedPack(final JFrame parent) {
    ModPack modPack = new ModPack();
    modPack.setMinecraftVersion((String) mcVersionBox.getSelectedItem());
    modPack.setForgeVersion(modPack.getMinecraftVersion()+"-"+(String) forgeVersionBox.getSelectedItem());
    modPack.setVersion(ModpackBuilder.instance.versionField.getText());
    modPack.setReleaseTime(System.currentTimeMillis());
    WorkDialog dialog = new WorkDialog(parent);
    dialog.start(modPack);
    //compareAndSetOptions(newestPack, modPack);
    final JDialog showFilesDialog = new JDialog(parent);
    final ModpackTableModel mTableModel = new ModpackTableModel(modPack);
    final JTable table = new JTable(mTableModel);
    table.setLayout(new BorderLayout());
    JScrollPane tableView = new JScrollPane(table);
    JButton removeButton = new JButton("Remove selected entry/entries");
    JButton doneButton = new JButton("Done");
    JButton refreshButton = new JButton("Refresh");
    JButton clientSide = new JButton("Toggle clientside");
    clientSide.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int[] rows = table.getSelectedRows();
        mTableModel.toggleClientSide(rows);
      }
    });
    refreshButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        WorkDialog dialog = new WorkDialog(parent);
        dialog.start(mTableModel.getPack());
        compareAndSetOptions(newestPack, mTableModel.getPack());
        mTableModel.fireTableDataChanged();
      }
    });
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
      }
    });
    Font font = new Font("Monospaced", Font.PLAIN, 12);
    table.setFont(font);
    JPanel bottomButtonPanel = new JPanel(new BorderLayout());
    bottomButtonPanel.add(removeButton, BorderLayout.NORTH);
    bottomButtonPanel.add(clientSide, BorderLayout.CENTER);
    bottomButtonPanel.add(doneButton, BorderLayout.SOUTH);
    showFilesDialog.add(refreshButton, BorderLayout.NORTH);
    showFilesDialog.add(bottomButtonPanel, BorderLayout.SOUTH);
    showFilesDialog.add(tableView, BorderLayout.CENTER);
    showFilesDialog.pack();
    showFilesDialog.setMinimumSize(new Dimension(700, getHeight()));
    showFilesDialog.setLocationRelativeTo(null);
    showFilesDialog.setModal(true);
    showFilesDialog.setVisible(true);
    try {
      JsonWriter.saveModpack(modPack, new File("modpack.json"));
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    UploadFileUtils.placeUploadFiles(ModpackBuilder.modpackDirectory.getAbsolutePath(),
        modPack.getFiles());
    System.exit(0);
  }

  public static void compareAndSetOptions(ModPack compareTo, ModPack setOptionsFor) {
    for (Map.Entry<String, ModPackFile> entry : setOptionsFor.getFiles().entrySet()) {
      if (compareTo.getFileByPath(entry.getKey()) != null) {
        ModPackFile compareToFile = compareTo.getFileByPath(entry.getKey());
        entry.getValue().setOptional(compareToFile.isOptional());
        entry.getValue().setSide(compareToFile.getSide());
      }
    }
  }



  public static class ModpackTableModel extends AbstractTableModel {

    @Getter
    ModPack pack;

    public ModpackTableModel(ModPack pack) {
      this.pack = pack;
    }


    @Override
    public int getColumnCount() {
      return 3;
    }


    @Override
    public String getColumnName(int arg0) {
      switch (arg0) {
        case 0:
          return "Filename";
        case 1:
          return "Side";
        case 2:
          return "MD5-Hash";
        default:
          return "Unkown";
      }
    }

    public void toggleClientSide(int[] rows) {
      ArrayList<String> names = Lists.newArrayList();
      for (int row : rows) {
        names.add((String) pack.getFiles().keySet().toArray()[row]);
      }
      for (String name : names) {
        ModPackFile packFile = pack.getFileByPath(name);
        switch (packFile.getSide()) {
          case CLIENT:
            packFile.setSide(Side.UNIVERSAL);
            break;
          case UNIVERSAL:
            packFile.setSide(Side.CLIENT);
            break;
          case SERVER:
            break;
          default:
            break;
        }
      }
      fireTableRowsUpdated(rows[0], rows[rows.length - 1]);
    }

    @Override
    public int getRowCount() {
      return pack.getFiles().size();
    }

    public void removeFile(int row) {
      removeFile((String) pack.getFiles().keySet().toArray()[row]);
    }

    public void removeFiles(int[] rows) {
      int removedRows = 0;
      for (int row : rows) {
        System.out.printf("Removing row #%d\n", row);
        removeFile(row - removedRows);
        removedRows++;
      }
      fireTableRowsDeleted(rows[0], rows[rows.length - 1]);
    }

    public void removeFile(String name) {
      System.out.printf("Removing %s\n", name);
      pack.getFiles().remove(name);
    }

    @Override
    public Object getValueAt(int row, int column) {
      switch (column) {
        case 0:
          return pack.getFiles().keySet().toArray()[row];
        case 1:
          return ((ModPackFile) pack.getFiles().values().toArray()[row]).getSide();
        case 2:
          return ((ModPackFile) pack.getFiles().values().toArray()[row]).getHash();
        default:
          return null;
      }
    }

    @Override
    public boolean isCellEditable(int t, int t2) {
      return false;
    }

  }

  public static boolean getModpackDirectory(Component parent) {
    JFileChooser fileChooser = new JFileChooser(new File("."));
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
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

  public static void addSaveHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          JsonWriter.saveBuilderSettings(ModpackBuilder.settings, new File(OSUtils.getLocalDir(),
              "builder.settings"));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }));
  }

  public static List<String> getMCVersions() {
    List<String> versions = Lists.newArrayList();
    try {

      for (MCVersionIndex.MCVersion v : mcVersionIndex.versions) {
        if (v.type.equals("release")) {
          versions.add(v.id);
        }
      }
    } catch (Exception e) {
      // IGNORE FOR NOW
      e.printStackTrace();
    }
    return versions;
  }

  public static List<String> getForgeForMC(String mc_version) {
    List<String> versions = Lists.newArrayList();
    Document d = Jsoup.parse(forgeVersionIndex);
    Element e = d.getElementById(mc_version + "_builds");
    Elements elem = e.getElementsByTag("td");
    Pattern p = Pattern.compile("\\d*\\.\\d*\\.\\d*\\.\\d*");
    for (Element e1 : elem) {
      String text = e1.ownText();
      if (!text.equals(mc_version)) {
        Matcher m = p.matcher(text);
        if (m.matches()) {
          versions.add(text);
        }
      }
    }
    return versions;
  }



}
