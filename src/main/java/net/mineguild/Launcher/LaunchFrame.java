package net.mineguild.Launcher;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lombok.Setter;
import net.miginfocom.swing.MigLayout;
import net.mineguild.Builder.ModpackBuilder;
import net.mineguild.Launcher.download.DownloadInfo;
import net.mineguild.Launcher.download.MultithreadedDownloadDialog;
import net.mineguild.Launcher.log.Console;
import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.minecraft.LoginDialog;
import net.mineguild.Launcher.minecraft.MCInstaller;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.json.JsonFactory;
import net.mineguild.Launcher.utils.json.JsonWriter;
import net.mineguild.Launcher.utils.json.Settings;
import net.mineguild.Launcher.utils.json.Settings.JavaSettings;
import net.mineguild.ModPack.ModPack;
import net.mineguild.ModPack.ModPackInstaller;
import net.mineguild.ModPack.Side;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class LaunchFrame extends JFrame {

  private JPanel contentPane;
  private JTextField launchPathField;
  private JTextField gameDirField;
  private JTextField javaPathField;
  private JLabel localVersion;
  private JLabel lastestVersion;
  private ModPack remotePack;
  private ModPack localPack;
  private JCheckBox chckbxForceUpdate;
  private JButton btnUpdateModpack;
  private JButton btnLaunch;
  private boolean needsUpdate = false;
  private @Setter boolean crashed = false;
  private JSlider memSlider;
  private JComboBox<String> permGenBox;
  private JCheckBox optimizationBox;


  /**
   * Create the frame.
   */
  public LaunchFrame() {
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        saveSettings();
      }
    });
    setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/icon.png")));
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    contentPane = new JPanel();
    contentPane.setBorder(null);
    contentPane.setLayout(new BorderLayout(0, 0));
    setContentPane(contentPane);

    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    tabbedPane.setBorder(null);
    contentPane.add(tabbedPane, BorderLayout.CENTER);

    JPanel mainPanel = new JPanel();
    tabbedPane.addTab("Update/Launch", null, mainPanel, null);
    mainPanel.setLayout(new MigLayout("", "[fill][grow][center]", "[grow][][][][grow][][]"));

    JLabel lblLogo = new JLabel("");
    lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
    mainPanel.add(lblLogo, "cell 0 0 3 1,alignx center,aligny center");
    lblLogo.setIcon(createImageIcon("/Logo.png", "MG LOGO"));

    JLabel lblInstalledVersion = new JLabel("Installed Version:");
    lblInstalledVersion.setHorizontalAlignment(SwingConstants.RIGHT);
    mainPanel.add(lblInstalledVersion, "flowx,cell 0 1");

    localVersion = new JLabel("-");
    mainPanel.add(localVersion, "cell 1 1");

    JButton btnRefreshLogin = new JButton("Refresh Login");
    btnRefreshLogin.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        LoginDialog d = new LoginDialog(MineguildLauncher.getLFrame());
        d.trySilentLogin();
        saveSettings();
      }
    });
    mainPanel.add(btnRefreshLogin, "cell 2 1");

    JLabel lblLatestVersion = new JLabel("Latest Version:");
    lblLatestVersion.setHorizontalAlignment(SwingConstants.RIGHT);
    mainPanel.add(lblLatestVersion, "flowx,cell 0 2");

    lastestVersion = new JLabel("-");
    mainPanel.add(lastestVersion, "cell 1 2");

    JButton btnLogoutchangeAccount = new JButton("Change Account");
    btnLogoutchangeAccount.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MineguildLauncher.res = null;
        LoginDialog d = new LoginDialog(MineguildLauncher.getLFrame());
        d.setReLogin();
        d.setVisible(true);
        saveSettings();
      }
    });
    mainPanel.add(btnLogoutchangeAccount, "cell 2 2");

    chckbxForceUpdate = new JCheckBox("Force Update");
    chckbxForceUpdate.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JCheckBox source = (JCheckBox) e.getSource();
        if (source.isSelected()) {
          getBtnUpdateModpack().setEnabled(true);
        } else {
          if (!needsUpdate) {
            getBtnUpdateModpack().setEnabled(false);
          }
        }
      }
    });
    mainPanel.add(chckbxForceUpdate, "cell 0 3");


    btnUpdateModpack = new JButton("Update ModPack");
    btnUpdateModpack.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveSettings();

        Thread t = new Thread(new Runnable() {

          @Override
          public void run() {
            getBtnLaunch().setEnabled(false);
            getBtnUpdateModpack().setEnabled(false);
            doUpdate();
          }
        });
        t.start();
      }
    });

    JButton btnRedoVersioncheck = new JButton("Check version");
    btnRedoVersioncheck.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveSettings();
        doVersionCheck();
      }
    });
    mainPanel.add(btnRedoVersioncheck, "cell 2 3");
    btnUpdateModpack.setEnabled(false);
    mainPanel.add(btnUpdateModpack, "cell 0 5 3 1");

    btnLaunch = new JButton("Launch");
    btnLaunch.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveSettings();
        launchMC();
      }
    });
    mainPanel.add(btnLaunch, "cell 0 6 3 1");

    JPanel settingsPanel = new JPanel();
    tabbedPane.addTab("Settings", null, settingsPanel, null);
    settingsPanel.setLayout(new FormLayout(new ColumnSpec[] {FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("right:default"), FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC,}, new RowSpec[] {FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),}));

    JLabel lblGeneralSettings =
        DefaultComponentFactory.getInstance().createTitle("General Settings");
    settingsPanel.add(lblGeneralSettings, "2, 2, left, default");

    JSeparator separator_1 = new JSeparator();
    settingsPanel.add(separator_1, "3, 2, 4, 1");

    JLabel lblMinecraftFilesPath =
        DefaultComponentFactory.getInstance().createLabel("Minecraft Files Path");
    lblMinecraftFilesPath.setHorizontalAlignment(SwingConstants.RIGHT);
    settingsPanel.add(lblMinecraftFilesPath, "2, 4, right, default");

    launchPathField = new JTextField();
    settingsPanel.add(launchPathField, "4, 4, fill, default");
    launchPathField.setColumns(10);

    JButton browseLaunchPathBtn = new JButton("Browse...");
    browseLaunchPathBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        launchPathField.setText(selectPath(launchPathField.getText()));
      }
    });
    settingsPanel.add(browseLaunchPathBtn, "6, 4");

    JLabel lblInstancePath = DefaultComponentFactory.getInstance().createLabel("Instance Path");
    lblInstancePath.setHorizontalAlignment(SwingConstants.RIGHT);
    settingsPanel.add(lblInstancePath, "2, 6, right, default");

    gameDirField = new JTextField();
    settingsPanel.add(gameDirField, "4, 6, fill, default");
    gameDirField.setColumns(10);

    JButton browseGameDirBtn = new JButton("Browse...");
    browseGameDirBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        gameDirField.setText(selectPath(gameDirField.getText()));
      }
    });
    settingsPanel.add(browseGameDirBtn, "6, 6");

    JLabel lblJavaSettings = DefaultComponentFactory.getInstance().createTitle("Java Settings");
    settingsPanel.add(lblJavaSettings, "2, 8, left, default");

    JSeparator separator = new JSeparator();
    settingsPanel.add(separator, "3, 8, 4, 1");

    JLabel lblJavaPath = DefaultComponentFactory.getInstance().createLabel("Java Path");
    settingsPanel.add(lblJavaPath, "2, 10, right, default");

    javaPathField = new JTextField();
    javaPathField.setColumns(10);
    settingsPanel.add(javaPathField, "4, 10, fill, default");

    JButton autoDetectJavaBtn = new JButton("Auto-Detect");
    autoDetectJavaBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        javaPathField.setText(MCInstaller.getDefaultJavaPath());
      }
    });
    settingsPanel.add(autoDetectJavaBtn, "6, 10");

    final JLabel lblMemory = DefaultComponentFactory.getInstance().createLabel("Memory");
    settingsPanel.add(lblMemory, "2, 12");

    memSlider = new JSlider();
    memSlider.setSnapToTicks(true);
    memSlider.setPaintTicks(true);
    memSlider.setValue(1);
    memSlider.setMinimum(1);
    memSlider.setMaximum((int) OSUtils.getOSTotalMemory() / 512);
    memSlider.setMinorTickSpacing(1);
    memSlider.setMajorTickSpacing(2);

    memSlider.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        lblMemory.setText("Memory(" + ((float) source.getValue() * 512) / 1024.0 + "gb)");
      }
    });
    settingsPanel.add(memSlider, "4, 12");

    JLabel lblPermgen = DefaultComponentFactory.getInstance().createLabel("PermGen");
    settingsPanel.add(lblPermgen, "2, 14, right, default");

    permGenBox = new JComboBox<String>();
    permGenBox.setModel(new DefaultComboBoxModel<String>(new String[] {"128m", "192m", "256m",
        "512m", "1024m"}));
    settingsPanel.add(permGenBox, "4, 14, fill, default");

    JLabel lblOptimizationArgs =
        DefaultComponentFactory.getInstance().createLabel("Optimization Args");
    settingsPanel.add(lblOptimizationArgs, "2, 16");

    optimizationBox = new JCheckBox("Use optimization arguments");
    settingsPanel.add(optimizationBox, "4, 16");
    if (MineguildLauncher.getSettings() == null) {
      MineguildLauncher.loadSettings();
      MineguildLauncher.addSaveHook();
    }
    if (MineguildLauncher.con == null) {
      EventQueue.invokeLater(new Runnable() {
        public void run() {
          try {
            MineguildLauncher.con = new Console();
            MineguildLauncher.con.setVisible(true);
            Logger.addListener(MineguildLauncher.con);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      });
    }
    if (MineguildLauncher.res == null) {
      LoginDialog d = new LoginDialog(this);
      d.setReLogin();
      d.trySilentLogin();
      if (d.successfull) {
        MineguildLauncher.res = d.response;
      } else {
        System.exit(0);
      }
    }
    pack();
  }

  public void loadSettings() {
    Settings set = MineguildLauncher.getSettings();
    JavaSettings jSet = set.getJavaSettings();
    // Launcher settings
    gameDirField.setText(set.getInstancePath().getAbsolutePath());
    launchPathField.setText(set.getLaunchPath().getAbsolutePath());
    if (set.getLastSize() != null) {
      setSize(set.getLastSize());
    }
    if (set.getLastLocation() != null) {
      setLocation(set.getLastLocation());
    }
    // Java settings
    permGenBox.setSelectedItem(jSet.getPermGen());
    memSlider.setValue(jSet.getMaxMemory() / 512);
    javaPathField.setText(jSet.getJavaPath());
    optimizationBox.setSelected(jSet.isOptimizationArgumentsUsed());
  }

  public void saveSettings() {
    Settings set = MineguildLauncher.getSettings();
    JavaSettings jSet = set.getJavaSettings();
    // Launcher settings
    set.setInstancePath(new File(gameDirField.getText()));
    set.setLaunchPath(new File(launchPathField.getText()));
    set.setLastLocation(getLocation());
    set.setLastSize(getSize());
    // Java Settings
    jSet.setPermGen((String) permGenBox.getSelectedItem());
    jSet.setMaxMemory(memSlider.getValue() * 512);
    jSet.setJavaPath(javaPathField.getText());
    jSet.setOptimizationArgumentsUsed(optimizationBox.isSelected());
    MineguildLauncher.saveSettingsSilent();
  }

  public void doVersionCheck() {
    getBtnLaunch().setEnabled(true);
    try {
      FileUtils.copyURLToFile(new URL(Constants.MG_MMP + "modpack.json"),
          new File(OSUtils.getLocalDir(), "newest.json"));
      remotePack = JsonFactory.loadModpack(new File(OSUtils.getLocalDir(), "newest.json"));
      File localPackFile =
          new File(MineguildLauncher.getSettings().getInstancePath(), "currentPack.json");
      localPack = null;
      try {
        localPack = JsonFactory.loadModpack(localPackFile);
        getLocalVersion().setText(localPack.getVersion());
      } catch (Exception e) {
        localPackFile.delete();
        Logger.logInfo("Unable to load current ModPack! Fresh-Install!");
      }
      if (!localPackFile.exists()) {
        getChckbxForceUpdate().setSelected(true);
        getChckbxForceUpdate().setEnabled(false);
        getBtnUpdateModpack().setEnabled(true);
        getBtnLaunch().setEnabled(false);
        needsUpdate = true;
      } else if (remotePack.isNewer(localPack)) {
        getBtnUpdateModpack().setEnabled(true);
        needsUpdate = true;
      }
      getLastestVersion().setText(remotePack.getVersion());
    } catch (Exception e) {
      Logger.logError("Error during versioncheck!", e);
    }
  }

  public void doUpdate() {
    boolean updated = true;
    MineguildLauncher.forceUpdate = getChckbxForceUpdate().isSelected();
    Logger.logInfo(String.format("Remote: %s [Released: %s] [Hash: %s]", remotePack.getVersion(),
        remotePack.getReleaseDate(), remotePack.getHash()));
    Logger.logInfo("Updating to Remote");
    File modsDir = new File(MineguildLauncher.getSettings().getInstancePath(), "mods");
    try {
      ModPackInstaller.clearFolder(modsDir, remotePack, Side.CLIENT, null);
    } catch (IOException e) {
      Logger.logError("Couldn't clear folder!", e);
    }
    List<DownloadInfo> dlinfo = Lists.newArrayList();
    try {
      dlinfo =
          ModPackInstaller.checkNeededFiles(MineguildLauncher.getSettings().getInstancePath(),
              remotePack, Side.CLIENT);
    } catch (Exception e) {
      Logger.logError("Error during ModPack hash checking!", e);
    }
    MultithreadedDownloadDialog dlDialog =
        new MultithreadedDownloadDialog(dlinfo, "Updating ModPack", this);
    dlDialog.setVisible(true);
    if (!dlDialog.run()) {
      Logger.logError("No success downloading!");
      updated = false;
      JOptionPane.showMessageDialog(this, "Updating didn't finish!", "Update error!",
          JOptionPane.ERROR_MESSAGE);
    } else {
      localPack = remotePack;
      try {
        JsonWriter.saveModpack(localPack, new File(MineguildLauncher.getSettings()
            .getInstancePath(), "currentPack.json"));
      } catch (IOException e) {
        Logger.logError("Unable to save pack!", e);
      }
    }
    if (updated) {
      getChckbxForceUpdate().setSelected(false);
      doVersionCheck();
    }
  }

  public void launchMC() {
    Thread t = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          getBtnLaunch().setEnabled(false);
          getBtnUpdateModpack().setEnabled(false);
          MCInstaller.setup(localPack, MineguildLauncher.getSettings().getLaunchPath(),
              MineguildLauncher.getSettings().getInstancePath(), MineguildLauncher.getSettings()
                  .getJavaSettings(), MineguildLauncher.res, true);
          setVisible(false);
        } catch (Exception e) {
          Logger.logError("Unable to launch!", e);
          JOptionPane.showMessageDialog(MineguildLauncher.getLFrame(),
              "Couldn't launch MC!\n" + e.getLocalizedMessage(), "Error launching MC!",
              JOptionPane.ERROR_MESSAGE);
          setVisible(true);
          doVersionCheck();
        }
      }
    });
    t.start();

  }

  public void mcStopped() {
    setVisible(true);
    if (crashed) {
      JOptionPane
          .showMessageDialog(this,
              "It seems like Minecraft crashed!\nMaybe try to up your memory settings (PermGen&Memory) a bit.");
      crashed = false;
    }
    doVersionCheck();
  }

  public JLabel getLocalVersion() {
    return localVersion;
  }

  public JLabel getLastestVersion() {
    return lastestVersion;
  }

  public JCheckBox getChckbxForceUpdate() {
    return chckbxForceUpdate;
  }

  public JButton getBtnUpdateModpack() {
    return btnUpdateModpack;
  }

  public JButton getBtnLaunch() {
    return btnLaunch;
  }

  public JTextField getTextField() {
    return javaPathField;
  }

  public JSlider getMemSlider() {
    return memSlider;
  }

  public JComboBox<String> getPermGenBox() {
    return permGenBox;
  }

  public String selectPath(String currentPath) {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int result = chooser.showOpenDialog(this);

    if (result == JFileChooser.APPROVE_OPTION) {
      return chooser.getSelectedFile().getAbsolutePath();
    }
    return currentPath;
  }

  public JCheckBox getOptimizationBox() {
    return optimizationBox;
  }

  protected ImageIcon createImageIcon(String path, String description) {
    java.net.URL imgURL = ModpackBuilder.class.getResource(path);
    if (imgURL != null) {
      return new ImageIcon(imgURL, description);
    } else {
      System.err.println("Couldn't find file: " + path);
      return null;
    }
  }
}