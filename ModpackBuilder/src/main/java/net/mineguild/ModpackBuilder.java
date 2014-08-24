package net.mineguild;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.CellConstraints.Alignment;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Size;

import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.Map;

@SuppressWarnings("serial")
public class ModpackBuilder extends JFrame {

  public static File modpackDirectory;
  JButton selectFolderButton;
  JButton startButton;
  JTextField pathField;
  JTextField versionField;
  JPanel sndPanel;
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
    sndPanel = builder.getPanel();
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
    add(sndPanel, BorderLayout.CENTER);
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

  public void createUpdatedPack(Component parent) {

  }

  public static void main(String[] args) throws Exception {
    try {
      UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
    } catch (Exception e) {
      e.printStackTrace();
    }
    new ModpackBuilder();
    /*
     * System.exit(0); //getModpackDirectory(); Modpack newPack = new Modpack(modpackDirectory);
     * File modpack_json = new File("newer_test.json"); Modpack oldPack =
     * Modpack.fromJson(FileUtils.readFileToString(new File("new_test.json")));
     * newPack.addModpackFiles(); Gson g = new GsonBuilder().setPrettyPrinting().create();
     * FileUtils.write(modpack_json, newPack.toJson());
     * System.out.println(g.toJson(oldPack.getNew(newPack)));
     * //fromUploadFiles(newPack.getModpackFiles());
     * placeUploadFiles(modpackDirectory.getAbsolutePath(), oldPack.getNew(newPack));
     */
    // System.out.println(g.toJson(Modpack.getOld(oldPack, newPack)));
    /*
     * Modpack m = new Modpack(); m.setReleaseTime(System.currentTimeMillis()); List<File> list =
     * (List<File>) FileUtils.listFiles(new File("testPack"), FileFilterUtils
     * .notFileFilter(FileFilterUtils.suffixFileFilter(".dis")), FileFilterUtils.trueFileFilter());
     * m.addModpackFiles(ChecksumUtil.getChecksum(list)); FileUtils.write(new File("new_test.json"),
     * m.toJson(), false);
     */

  }

  public static void placeUploadFiles(String basePath, Map<String, String> files) {
    File uploadDir = new File("upload");
    uploadDir.mkdir();
    try {
      FileUtils.cleanDirectory(uploadDir);
    } catch (IOException e) {
      e.printStackTrace();
    }
    for (Map.Entry<String, String> entry : files.entrySet()) {
      File file = new File(basePath, entry.getKey());
      File newDirectory = new File(uploadDir, entry.getValue().substring(0, 2));
      File newFile = new File(newDirectory, entry.getValue());
      if (file.exists()) {
        try {
          FileUtils.copyFile(file, newFile);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void fromUploadFiles(Map<String, String> files) {
    File modpack = new File("modpack");
    File upload = new File("upload");
    modpack.mkdir();
    try {
      FileUtils.cleanDirectory(modpack);
    } catch (IOException ignored) {
    }
    for (Map.Entry<String, String> entry : files.entrySet()) {
      String hash = entry.getValue();
      File fileDir = new File(upload, hash.substring(0, 2));
      File file = new File(fileDir, hash);
      String path = entry.getKey();
      File filePath = new File(modpack, path);
      try {
        System.out.printf("Copying %s to %s\n", file.toString(), filePath.toString());
        FileUtils.copyFile(file, filePath);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static boolean getModpackDirectory(Component parent) {
    JFileChooser fileChooser = new JFileChooser(new File("."));
    FileNameExtensionFilter filter = new FileNameExtensionFilter("Modpack_Json", "json", "mmp");
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
