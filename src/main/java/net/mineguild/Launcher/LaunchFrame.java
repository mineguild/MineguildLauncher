package net.mineguild.Launcher;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
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
  private JSpinner bufferSizeSpinner;
  private final String[] permGenSizes = new String[] {"192m", "256m", "512m", "1024m"};
  private JLabel localDirLabel;


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
    setTitle("Mineguild Launcher");
    contentPane = new JPanel();
    contentPane.setBorder(null);
    contentPane.setLayout(new BorderLayout(0, 0));
    setContentPane(contentPane);

    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    tabbedPane.setFocusable(false);
    tabbedPane.setBorder(null);
    contentPane.add(tabbedPane, BorderLayout.CENTER);

    JPanel mainPanel = new JPanel();
    mainPanel.setBorder(null);
    tabbedPane.addTab("Update/Launch", null, mainPanel, null);
    mainPanel.setLayout(new MigLayout("", "[center][grow][center]", "[grow][][][][][][]"));

    JLabel lblLogo = new JLabel("");
    lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
    mainPanel.add(lblLogo, "cell 0 0 3 1,alignx center,aligny center");
    lblLogo.setIcon(createImageIcon("/Logo.png", "MG LOGO"));
    lblLogo.setToolTipText("Open Mineguild Homepage");
    lblLogo.setCursor(new Cursor(Cursor.HAND_CURSOR));
    lblLogo.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {
        try {
          Desktop.getDesktop().browse(new URI("https://mineguild.net"));
        } catch (Exception e1) {
          Logger.logError("Can't open MG Homepage!", e1);
        }
      }

    });
    JLabel lblInstalledVersion = new JLabel("Installed Version:");
    lblInstalledVersion.setHorizontalAlignment(SwingConstants.RIGHT);
    mainPanel.add(lblInstalledVersion, "flowx,cell 0 1");

    localVersion = new JLabel("-");
    mainPanel.add(localVersion, "cell 1 1");

    JButton btnRefreshLogin = new JButton("Refresh Login");
    btnRefreshLogin.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        refreshLogin();
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
        MineguildLauncher.res = d.response;
        saveSettings();
      }
    });
    mainPanel.add(btnLogoutchangeAccount, "cell 2 2");


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

    JLabel lblLocalDirectory = new JLabel("Local Directory:");
    mainPanel.add(lblLocalDirectory, "cell 0 3");

    localDirLabel =
        new JLabel("<html><FONT color=\"#000099\"><U>" + OSUtils.getLocalDir().getAbsolutePath()
            + "</U></FONT></html>");
    localDirLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    localDirLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {

        try {
          Desktop.getDesktop().open(OSUtils.getLocalDir());
        } catch (IOException e1) {
          Logger.logError("Unable to open local dir!", e1);
        }

      }
    });
    mainPanel.add(localDirLabel, "cell 1 3");
    mainPanel.add(btnRedoVersioncheck, "cell 2 3");

    JButton btnOpenBuilder = new JButton("Open Builder");
    btnOpenBuilder.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          setVisible(false);
          ModpackBuilder.launch(MineguildLauncher.getSettings().getBuilderSettings());
        } catch (Exception e1) {
          e1.printStackTrace();
        }
      }
    });

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
    mainPanel.add(chckbxForceUpdate, "cell 0 4");
    mainPanel.add(btnOpenBuilder, "cell 2 4");
    btnUpdateModpack.setEnabled(false);
    mainPanel.add(btnUpdateModpack, "cell 0 5 3 1,growx");

    btnLaunch = new JButton("Launch");
    btnLaunch.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveSettings();
        launchMC();
      }
    });
    mainPanel.add(btnLaunch, "cell 0 6 3 1,growx");

    JPanel settingsPanel = new JPanel();
    settingsPanel.setBorder(null);
    tabbedPane.addTab("Settings", null, settingsPanel, null);
    settingsPanel.setLayout(new FormLayout(
        new ColumnSpec[] {FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("right:default"),
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
            FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
            FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,}, new RowSpec[] {
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),}));

    JLabel lblGeneralSettings =
        DefaultComponentFactory.getInstance().createTitle("General Settings");
    settingsPanel.add(lblGeneralSettings, "2, 2, left, default");

    JSeparator separator_1 = new JSeparator();
    settingsPanel.add(separator_1, "3, 2, 6, 1");

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

    JButton mcFilesOpen = new JButton("Open..");
    settingsPanel.add(mcFilesOpen, "6, 4");
    settingsPanel.add(browseLaunchPathBtn, "8, 4");

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

    JButton instancePathOpen = new JButton("Open..");
    settingsPanel.add(instancePathOpen, "6, 6");
    settingsPanel.add(browseGameDirBtn, "8, 6");

    JLabel lblConsoleBufferSize =
        DefaultComponentFactory.getInstance().createLabel("Console Buffer Size");
    settingsPanel.add(lblConsoleBufferSize, "2, 8");

    bufferSizeSpinner = new JSpinner();
    bufferSizeSpinner.setModel(new SpinnerNumberModel(new Long(0), new Long(0), null, new Long(1)));
    settingsPanel.add(bufferSizeSpinner, "4, 8, 3, 1");

    JLabel lblJavaSettings = DefaultComponentFactory.getInstance().createTitle("Java Settings");
    settingsPanel.add(lblJavaSettings, "2, 10, left, default");

    JSeparator separator = new JSeparator();
    settingsPanel.add(separator, "3, 10, 6, 1");

    JLabel lblJavaPath = DefaultComponentFactory.getInstance().createLabel("Java Path");
    settingsPanel.add(lblJavaPath, "2, 12, right, default");

    javaPathField = new JTextField();
    javaPathField.setColumns(10);
    settingsPanel.add(javaPathField, "4, 12, 3, 1, fill, default");

    JButton autoDetectJavaBtn = new JButton("Auto-Detect");
    autoDetectJavaBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        javaPathField.setText(MCInstaller.getDefaultJavaPath());
      }
    });
    settingsPanel.add(autoDetectJavaBtn, "8, 12");

    final JLabel lblMemory = DefaultComponentFactory.getInstance().createLabel("Memory");
    settingsPanel.add(lblMemory, "2, 14");

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
    settingsPanel.add(memSlider, "4, 14, 3, 1");

    JLabel lblPermgen = DefaultComponentFactory.getInstance().createLabel("PermGen");
    settingsPanel.add(lblPermgen, "2, 16, right, default");

    permGenBox = new JComboBox<String>();
    permGenBox.setModel(new DefaultComboBoxModel<String>(permGenSizes));
    settingsPanel.add(permGenBox, "4, 16, 3, 1, fill, default");

    JLabel lblOptimizationArgs =
        DefaultComponentFactory.getInstance().createLabel("Optimization Args");
    settingsPanel.add(lblOptimizationArgs, "2, 18");

    optimizationBox = new JCheckBox("Use optimization arguments");
    settingsPanel.add(optimizationBox, "4, 18, 3, 1");
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
    setMinimumSize(getSize());
  }

  public void loadSettings() {
    Settings set = MineguildLauncher.getSettings();
    JavaSettings jSet = set.getJavaSettings();
    // Launcher settings
    gameDirField.setText(set.getInstancePath().getAbsolutePath());
    launchPathField.setText(set.getLaunchPath().getAbsolutePath());
    bufferSizeSpinner.setValue(set.getConsoleBufferSize());
    if (set.getLastSize() != null) {
      setSize(set.getLastSize());
    }
    if (set.getLastLocation() != null) {
      setLocation(set.getLastLocation());
    }

    // Java settings
    if (Lists.newArrayList(permGenSizes).contains(jSet.getPermGen())) {
      permGenBox.setSelectedItem(jSet.getPermGen());
    } else {
      jSet.setPermGen((String) permGenBox.getSelectedItem());
    }

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
    set.setConsoleBufferSize((Long) bufferSizeSpinner.getValue());
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
        getLocalVersion().setText(createVersionLabel(localPack));
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
      getLastestVersion().setText(createVersionLabel(remotePack));
    } catch (Exception e) {
      Logger.logError("Error during versioncheck!", e);
    }
  }

  public static String createVersionLabel(ModPack pack) {
    SimpleDateFormat format = new SimpleDateFormat();
    return String.format("<html><b>%s</b> - released %s [MC %s] [Forge %s]", pack.getVersion(),
        format.format(new Date(pack.getReleaseTime())), pack.getMinecraftVersion(), pack
            .getForgeVersion().split("-", 2)[1]);
  }

  public void doUpdate() {
    boolean updated = true;
    MineguildLauncher.forceUpdate = getChckbxForceUpdate().isSelected();
    Logger.logInfo(String.format("Remote: %s [Released: %s] [Hash: %s]", remotePack.getVersion(),
        remotePack.getReleaseDate(), remotePack.getHash()));
    Logger.logInfo("Updating to Remote");
    File modsDir = new File(MineguildLauncher.getSettings().getInstancePath(), "mods");
    File backupDirectory = null;
    try {
      int result =
          JOptionPane.showConfirmDialog(this, "Do you want to backup your locally modified files?",
              "Create backup?", JOptionPane.YES_NO_OPTION);
      if (result == JOptionPane.YES_OPTION) {
        backupDirectory = new File(MineguildLauncher.getSettings().getInstancePath(), "backup");
        if (backupDirectory.exists() && backupDirectory.isDirectory()) {
          FileUtils.cleanDirectory(backupDirectory);
        }
      }
      if (!MineguildLauncher.forceUpdate) {
        ModPackInstaller.clearFolder(MineguildLauncher.getSettings().getInstancePath(), localPack,
            remotePack, Side.CLIENT, backupDirectory);
      } else {
        ModPackInstaller.clearFolder(modsDir, remotePack, Side.CLIENT, backupDirectory);
      }
    } catch (IOException e) {
      Logger.logError("Couldn't clear folder!", e);
    }
    List<DownloadInfo> dlinfo = Lists.newArrayList();
    try {
      if (MineguildLauncher.forceUpdate) {
        dlinfo =
            ModPackInstaller.checkNeededFiles(MineguildLauncher.getSettings().getInstancePath(),
                remotePack, Side.CLIENT);
      } else {
        dlinfo =
            ModPackInstaller.checkNeededFiles(MineguildLauncher.getSettings().getInstancePath(),
                localPack, remotePack, Side.CLIENT);
      }
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
      if (backupDirectory != null) {
        JLabel component = new JLabel();
        final File _bkDir = backupDirectory;
        component.setCursor(new Cursor(Cursor.HAND_CURSOR));
        component.setText("<HTML>Backup was created in:<br><FONT color=\"#000099\"><U>"
            + backupDirectory.getAbsolutePath() + "</U></FONT></HTML>");
        component.addMouseListener(new MouseAdapter() {

          @Override
          public void mouseClicked(MouseEvent e) {
            try {
              Desktop.getDesktop().open(_bkDir);
            } catch (IOException e1) {
              Logger.logError("Unable to open backupDirectory!", e1);
            }
          }

        });
        JOptionPane.showMessageDialog(this, component, "Backup created.",
            JOptionPane.INFORMATION_MESSAGE);
      }
      getChckbxForceUpdate().setSelected(false);
      doVersionCheck();
    }
  }

  public void refreshLogin() {
    LoginDialog d = new LoginDialog(this);
    d.trySilentLogin();
    saveSettings();
  }

  public void launchMC() {
    if(MineguildLauncher.res.isStartedGame()){
      refreshLogin();
    }
    Thread t = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          getBtnLaunch().setEnabled(false);
          getBtnUpdateModpack().setEnabled(false);
          java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
              MineguildLauncher.con.toFront();
              MineguildLauncher.con.repaint();
            }
          });
          MCInstaller.setup(localPack, MineguildLauncher.getSettings().getLaunchPath(),
              MineguildLauncher.getSettings().getInstancePath(), MineguildLauncher.getSettings()
                  .getJavaSettings(), MineguildLauncher.res, true);
          setVisible(false);
          MineguildLauncher.res.setStartedGame(true);
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
    toFront();
    repaint();
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

  public JSpinner getBufferSizeSpinner() {
    return bufferSizeSpinner;
  }

}
