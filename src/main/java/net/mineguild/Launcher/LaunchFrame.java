package net.mineguild.Launcher;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

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
import net.mineguild.ModPack.ModPack;
import net.mineguild.ModPack.ModPackInstaller;
import net.mineguild.ModPack.ModPackVersion;
import net.mineguild.ModPack.ModpackRepository;
import net.mineguild.ModPack.ModpackRepository.VersionRepository;
import net.mineguild.ModPack.Side;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class LaunchFrame extends JFrame {

  private JPanel contentPane;
  private JLabel localVersion;
  private ModPack remotePack;
  private ModPack localPack;
  private JCheckBox chckbxForceUpdate;
  private JButton btnUpdateModpack;
  private JButton btnLaunch;
  private boolean needsUpdate = false;
  private @Setter boolean crashed = false;
  private final ImageIcon tickIcon = createImageIcon("/tick.png", "Tick Icon");
  private final ImageIcon crossIcon = createImageIcon("/cross.png", "Cross Icon");
  private JLabel localDirLabel;
  private JPanel mainPanel;
  private SettingsPanel settingsPanel;
  private JLabel lblUpdated;
  private JComboBox<VersionRepository> modpackSelection;
  private JComboBox<ModPackVersion> targetVersion;

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

    mainPanel = new JPanel();
    mainPanel.setBorder(null);
    tabbedPane.addTab("Launch", null, mainPanel, null);
    tabbedPane.setEnabledAt(0, true);
    mainPanel.setLayout(new MigLayout("", "[grow,center][grow][center]", "[][grow][][][][][][][]"));

    modpackSelection = new JComboBox<VersionRepository>();
    /*modpackSelection.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateGUI(true);
      }
    });*/
    modpackSelection.addItemListener(new ItemChangeListener());
    mainPanel.add(modpackSelection, "cell 1 0,growx");

    JLabel lblLogo = new JLabel("");
    lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
    mainPanel.add(lblLogo, "cell 0 1 3 1,alignx center,aligny center");
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
    mainPanel.add(lblInstalledVersion, "flowx,cell 0 2");

    localVersion = new JLabel("-");
    mainPanel.add(localVersion, "cell 1 2");

    JButton btnRefreshLogin = new JButton("Refresh Login");
    btnRefreshLogin.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        refreshLogin();
      }
    });
    mainPanel.add(btnRefreshLogin, "cell 2 2");

    JLabel lblLatestVersion = new JLabel("Target Version:");
    lblLatestVersion.setHorizontalAlignment(SwingConstants.RIGHT);
    mainPanel.add(lblLatestVersion, "flowx,cell 0 3");

    targetVersion = new JComboBox<ModPackVersion>();
    targetVersion.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateGUI(false);
      }
    });
    mainPanel.add(targetVersion, "cell 1 3,growx");

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
    mainPanel.add(btnLogoutchangeAccount, "cell 2 3");


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

    lblUpdated = new JLabel("");
    mainPanel.add(lblUpdated, "cell 1 4");

    JLabel lblLocalDirectory = new JLabel("Local Directory:");
    mainPanel.add(lblLocalDirectory, "cell 0 5");

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
    mainPanel.add(localDirLabel, "cell 1 5");
    mainPanel.add(btnRedoVersioncheck, "cell 2 5");

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
    mainPanel.add(chckbxForceUpdate, "cell 0 6");
    mainPanel.add(btnOpenBuilder, "cell 2 6");
    btnUpdateModpack.setEnabled(false);
    mainPanel.add(btnUpdateModpack, "cell 0 7 3 1,growx");

    btnLaunch = new JButton("Launch");
    btnLaunch.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveSettings();
        launchMC();
      }
    });
    mainPanel.add(btnLaunch, "cell 0 8 3 1,growx");

    settingsPanel = new SettingsPanel(MineguildLauncher.getSettings());

    tabbedPane.addTab("Settings", null, settingsPanel, null);
    tabbedPane.setEnabledAt(1, true);

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

  public void showDlThreadHelp() {
    JOptionPane
        .showMessageDialog(
            this,
            "Decrease this if you're having problems with corrupt files or breaking dl's. Increase if you want it to be faster (there's a limit).\n Can break the downloader if too high.",
            "Download Threads Information", JOptionPane.INFORMATION_MESSAGE);
  }

  public void loadSettings() {
    Settings set = MineguildLauncher.getSettings();
    pack();
    if (set.getLastSize() != null) {
      if (set.getLastSize().getHeight() > getHeight() && set.getLastSize().getWidth() > getWidth()) {
        setSize(set.getLastSize());
      }
    }
    if (set.getLastLocation() != null) {
      setLocation(set.getLastLocation());
    }
    settingsPanel.loadSettings();
  }

  public void saveSettings() {
    Settings set = MineguildLauncher.getSettings();
    set.setLastLocation(getLocation());
    settingsPanel.saveSettings();
    MineguildLauncher.saveSettingsSilent();
  }

  public void doVersionCheck() {
    MineguildLauncher.getRepositories().clear();

    for (String repoUrl : MineguildLauncher.getSettings().getRepositories()) {
      try {
        modpackSelection.removeAllItems();
        ModpackRepository updated = JsonFactory.loadRepository(repoUrl);
        MineguildLauncher.getRepositories().add(updated);
        for (ModpackRepository mRepo : MineguildLauncher.getRepositories()) {
          for (VersionRepository repo : mRepo.getPacks().values()) {

            modpackSelection.addItem(repo);
          }
          if (mRepo.getPacks().containsKey(MineguildLauncher.getSettings().getLastPack())) {
            modpackSelection.setSelectedItem(mRepo.getPacks().get(
                MineguildLauncher.getSettings().getLastPack()));
          }
        }
      } catch (Exception e) { // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    /*
     * try { ModpackRepository localRepo = JsonFactory.loadRepository(new
     * File("defaultrepository.json")); for (VersionRepository repo : localRepo.getPacks().values())
     * { modpackSelection.addItem(repo); }
     * 
     * if (localRepo.getPacks().containsKey(MineguildLauncher.getSettings().getLastPack())) {
     * modpackSelection.setSelectedItem(localRepo.getPacks().get(
     * MineguildLauncher.getSettings().getLastPack()));
     * 
     * } } catch (Exception e) { e.printStackTrace(); }
     */
    

    /*
     * try { FileUtils.copyURLToFile(new URL(Constants.MG_MMP + "modpack.json"), new
     * File(OSUtils.getLocalDir(), "newest.json")); remotePack = JsonFactory.loadModpack(new
     * File(OSUtils.getLocalDir(), "newest.json")); File localPackFile = new
     * File(MineguildLauncher.getSettings().getInstancePath(), "currentPack.json"); localPack =
     * null; int result = JOptionPane.NO_OPTION; try { localPack =
     * JsonFactory.loadModpack(localPackFile);
     * getLocalVersion().setText(createVersionLabel(localPack)); } catch (Exception e) {
     * localPackFile.delete(); Logger.logInfo("Unable to load current ModPack! Fresh-Install!"); }
     * if (!localPackFile.exists()) { getChckbxForceUpdate().setSelected(true);
     * getChckbxForceUpdate().setEnabled(false); getBtnUpdateModpack().setEnabled(true);
     * getBtnLaunch().setEnabled(false); lblUpdated.setIcon(crossIcon); needsUpdate = true; result =
     * JOptionPane.showConfirmDialog(this,
     * "The modpack needs to be re-installed! Do you want to install it now?", "Install ModPack",
     * JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); } else if
     * (remotePack.isNewer(localPack)) { getBtnUpdateModpack().setEnabled(true);
     * lblUpdated.setIcon(crossIcon); needsUpdate = true; result = JOptionPane.showConfirmDialog(
     * this, String.format("A new update (%s %s) is available! Update now?",
     * remotePack.getVersion(), remotePack.getReleaseDate()), "Update ModPack",
     * JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); } else {
     * lblUpdated.setIcon(tickIcon); } //setText(createVersionLabel(remotePack)); if (needsUpdate &&
     * result == JOptionPane.YES_OPTION) { saveSettings();
     * 
     * Thread t = new Thread(new Runnable() {
     * 
     * @Override public void run() { getBtnLaunch().setEnabled(false);
     * getBtnUpdateModpack().setEnabled(false); doUpdate(); } }); t.start(); } } catch (Exception e)
     * { Logger.logError("Error during versioncheck!", e); }
     */
  }

  public void updateGUI(boolean modpackChanged) {
    if (modpackChanged) {
      targetVersion.removeAllItems();
      VersionRepository repo = (VersionRepository) modpackSelection.getSelectedItem();
      LinkedList<ModPackVersion> list =
          new LinkedList<ModPackVersion>((LinkedHashSet) repo.getVersions());
      Iterator<ModPackVersion> itr = list.descendingIterator();
      while (itr.hasNext()) {
        ModPackVersion version = (ModPackVersion) itr.next();
        System.out.println(version.getVersion());
        targetVersion.addItem(version);
      }
    }
    getBtnLaunch().setEnabled(true);
    File localPackFile =
        new File(MineguildLauncher.getSettings().getInstancesPath(), ((VersionRepository) modpackSelection.getSelectedItem()).getName()
            + "/currentPack.json");
    if (!localPackFile.exists()) {
      btnUpdateModpack.setEnabled(true);
      chckbxForceUpdate.setEnabled(true);
      chckbxForceUpdate.setSelected(true);
      MineguildLauncher.forceUpdate = true;
      btnLaunch.setEnabled(false);
      needsUpdate = true;
      lblUpdated.setIcon(crossIcon);
      return;
    }
    try {
      localPack = JsonFactory.loadModpack(localPackFile);
      ModPackVersion selectedVersion = (ModPackVersion) targetVersion.getSelectedItem();
      if (selectedVersion.isNewer(localPack) || localPack.isNewer(selectedVersion)) {
        btnUpdateModpack.setEnabled(true);
        chckbxForceUpdate.setEnabled(true);
        lblUpdated.setIcon(crossIcon);
        
      } else {
        btnUpdateModpack.setEnabled(false);
        chckbxForceUpdate.setEnabled(true);
        MineguildLauncher.forceUpdate = true;
        needsUpdate = true;

        lblUpdated.setIcon(tickIcon);
      }
      localVersion.setText(createVersionLabel(localPack));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
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
    Logger.logInfo("Downloading Version information...");
    VersionRepository selectedRepo = (VersionRepository) modpackSelection.getSelectedItem();
    try {
      remotePack = selectedRepo.loadPack(((ModPackVersion) targetVersion.getSelectedItem()));
    } catch (IOException e2) {
      Logger.logError("Couldn't load remote pack!");
      return;
    }
    Logger.logInfo(String.format("Remote: %s [Released: %s] [Hash: %s]", remotePack.getVersion(),
        remotePack.getReleaseDate(), remotePack.getHash()));
    Logger.logInfo("Updating to Remote");
    File instancePath =
        new File(MineguildLauncher.getSettings().getInstancesPath(), selectedRepo.getName());
    File modsDir = new File(instancePath, "mods");
    File backupDirectory = null;
    try {
      int result = JOptionPane.NO_OPTION;
      if (localPack != null) {
        result =
            JOptionPane.showConfirmDialog(this,
                "Do you want to backup your locally modified files?", "Create backup?",
                JOptionPane.YES_NO_OPTION);
      }
      if (result == JOptionPane.YES_OPTION) {
        backupDirectory = new File(instancePath, "backup");
        if (backupDirectory.exists() && backupDirectory.isDirectory()) {
          FileUtils.cleanDirectory(backupDirectory);
        }
      }
      System.out.println(MineguildLauncher.forceUpdate);
      if (!MineguildLauncher.forceUpdate) {
        ModPackInstaller.clearFolder(instancePath, localPack, remotePack, Side.CLIENT,
            backupDirectory);
      } else {
        ModPackInstaller.clearFolder(modsDir, remotePack, Side.CLIENT, backupDirectory);
      }
    } catch (IOException e) {
      Logger.logError("Couldn't clear folder!", e);
    }
    List<DownloadInfo> dlinfo = Lists.newArrayList();
    try {
      if (MineguildLauncher.forceUpdate) {
        dlinfo = ModPackInstaller.checkNeededFiles(instancePath, remotePack, Side.CLIENT);
      } else {
        dlinfo = ModPackInstaller.checkNeededFiles(instancePath, remotePack, Side.CLIENT);
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
        JsonWriter.saveModpack(localPack, new File(instancePath, "currentPack.json"));
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
    MineguildLauncher.res = d.response;
    saveSettings();
  }

  public void launchMC() {
    if (MineguildLauncher.res.isStartedGame()) {
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
          File instancePath = new File(MineguildLauncher.getSettings().getInstancesPath(), ((VersionRepository)modpackSelection.getSelectedItem()).getName());
          MCInstaller.setup(localPack, MineguildLauncher.getSettings().getMinecraftResourcePath(),
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


  public JCheckBox getChckbxForceUpdate() {
    return chckbxForceUpdate;
  }

  public JButton getBtnUpdateModpack() {
    return btnUpdateModpack;
  }

  public JButton getBtnLaunch() {
    return btnLaunch;
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

  public JPanel getMainPanel() {
    return mainPanel;
  }

  public JLabel getLblUpdated() {
    return lblUpdated;
  }
  
  private class ItemChangeListener implements ItemListener{
    @Override
    public void itemStateChanged(ItemEvent event) {
       if (event.getStateChange() == ItemEvent.SELECTED) {
         updateGUI(true);
       }
    }       
}
}
