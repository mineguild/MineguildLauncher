package net.mineguild.Launcher;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import lombok.Getter;
import lombok.Setter;
import net.mineguild.Launcher.log.Console;
import net.mineguild.Launcher.log.LogSource;
import net.mineguild.Launcher.log.LogWriter;
import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.minecraft.LoginResponse;
import net.mineguild.Launcher.minecraft.ProcessMonitor;
import net.mineguild.Launcher.utils.AuthWorkDialog;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.json.JsonFactory;
import net.mineguild.Launcher.utils.json.JsonWriter;
import net.mineguild.Launcher.utils.json.Settings;
import net.mineguild.ModPack.ModPack;
import net.mineguild.ModPack.Side;


public class MineguildLauncher {

  public static File baseDirectory;
  public static boolean doExactCheck = true;
  public static boolean MCRunning;
  public static boolean forceUpdate;
  public static ProcessMonitor procmon;
  public static Console con;
  private static @Getter LaunchFrame lFrame;
  private static @Getter Frame parent;
  private static @Getter @Setter LogWriter mcLogger = null;
  public static long totalDownloadTime = 0;
  private static @Getter Settings settings;
  public static LoginResponse res;

  public static void main(String[] args) throws Exception {
    if (args.length >= 1) {
      if (args.length == 2) {
        forceUpdate = args[1].equals("--forceUpdate");
      }
      if (args[0].equals("updateServer")) {
        MineguildLauncherConsole.update(Side.SERVER);
      } else if (args[0].equals("updateClient")) {
        MineguildLauncherConsole.update(Side.CLIENT);
      } else if (args[0].equals("updateBoth")) {
        MineguildLauncherConsole.update(Side.BOTH);
      }
      System.exit(0);
    }
    // Logger.addListener(new StdOutLogger());
    mcLogger = new LogWriter(new File(OSUtils.getLocalDir(), "minecraft.log"), LogSource.EXTERNAL);
    Logger.addListener(new LogWriter(new File(OSUtils.getLocalDir(), "launcher.log"),
        LogSource.LAUNCHER));
    Logger
        .addListener(new LogWriter(new File(OSUtils.getLocalDir(), "combined.log"), LogSource.ALL));
    Logger.addListener(mcLogger);
    loadSettings();
    setNimbus();
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          AuthWorkDialog dl = new AuthWorkDialog(con);
          dl.start();
          lFrame = new LaunchFrame();
          lFrame.loadSettings();
          lFrame.setVisible(true);
          lFrame.doVersionCheck();
          parent = lFrame;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  public static File getInstallPath(Component par) {
    Component parent = (par == null) ? con : par;
    File folder;
    try {
      folder =
          MineguildLauncher.settings.getLaunchPath() == null ? new File("modpack")
              : MineguildLauncher.settings.getLaunchPath();
    } catch (NullPointerException e) {
      folder = new File("modpack");
    }
    int result =
        JOptionPane
            .showConfirmDialog(
                parent,
                String
                    .format(
                        "Do you want to select a different install location for the modpack?\nOtherwise it will be installed in %s",
                        folder.getAbsolutePath()), "Select MMP install location.",
                JOptionPane.YES_NO_OPTION);
    if (result == JOptionPane.OK_OPTION) {
      JFileChooser chooser = new JFileChooser();
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      result = chooser.showDialog(con, "Use selected directory");
      if (result == JFileChooser.APPROVE_OPTION) {
        return chooser.getSelectedFile();
      } else {
        return getInstallPath(parent);
      }

    } else {
      if (!folder.exists()) {
        if (folder.mkdirs()) {
          return folder;
        } else {
          return getInstallPath(parent);
        }
      } else if (folder.canWrite()) {
        return folder;
      } else {
        return getInstallPath(parent);
      }
    }
  }

  public static boolean needsUpdate(ModPack localPack, ModPack newestPack, boolean askUpdate) {

    Logger.logInfo(String.format("Local pack version: %s released on %s", localPack.getVersion(),
        localPack.getReleaseDate()));
    if (newestPack.isNewer(localPack)) {
      if (askUpdate) {
        int result =
            JOptionPane.showConfirmDialog(con, String.format(
                "A new version %s released on %s is available! Do you want to update?",
                newestPack.getVersion(), newestPack.getReleaseDate()), "Update modpack?",
                JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
          return true;
        } else {
          // We let him launch the pack although he won't be able to play on the server.
          Logger.logInfo("Pack wasn't updated, because user denied.");
        }
      } else {
        return true;
      }
    } else {
      Logger.logInfo("Not updating, because not newer");
    }

    return false;
  }

  public static void addSaveHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          JsonWriter.saveSettings(MineguildLauncher.settings, new File(OSUtils.getLocalDir(),
              "settings.json"));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }));
  }

  public static void loadSettings() {
    try {
      settings = JsonFactory.loadSettings(new File(OSUtils.getLocalDir(), "settings.json"));
    } catch (IOException e) {
      OSUtils.getLocalDir().mkdirs();
      settings = new Settings();
      try {
        JsonWriter.saveSettings(settings, new File(OSUtils.getLocalDir(), "settings.json"));
      } catch (IOException e1) {
        Logger.logError("Unable to write new settings!", e1);
      }
    }
  }

  public static void saveSettingsSilent() {
    try {
      JsonWriter.saveSettings(settings, new File(OSUtils.getLocalDir(), "settings.json"));
    } catch (IOException e) {
      Logger.logError("Unable to save settings!", e);
    }
  }

  public static void setNimbus() {
    try {
      for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          UIManager.setLookAndFeel(info.getClassName());
          // UIManager.put("nimbusBase", new Color(74, 1, 1));
          if (getSettings().isRedStyle()) {
            UIManager.put("nimbusBase", new Color(55, 0, 0));
            UIManager.put("nimbusBlueGrey", new Color(120, 1, 1));
            UIManager.put("control", Color.DARK_GRAY);
            UIManager.put("menu", Color.green);
            UIManager.put("nimbusLightBackground", new Color(28, 28, 28));
            UIManager.put("text", Color.white);
            UIManager.put("info", new Color(31, 31, 31));
          }

          break;
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void setSystem() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InstantiationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UnsupportedLookAndFeelException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
