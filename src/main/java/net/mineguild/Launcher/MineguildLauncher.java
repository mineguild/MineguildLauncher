package net.mineguild.Launcher;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.mineguild.Launcher.download.DownloadInfo;
import net.mineguild.Launcher.log.Console;
import net.mineguild.Launcher.log.LogSource;
import net.mineguild.Launcher.log.LogWriter;
import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.log.StdOutLogger;
import net.mineguild.Launcher.minecraft.LoginDialog;
import net.mineguild.Launcher.minecraft.LoginResponse;
import net.mineguild.Launcher.minecraft.MCInstaller;
import net.mineguild.Launcher.minecraft.ProcessMonitor;
import net.mineguild.Launcher.utils.AuthWorkDialog;
import net.mineguild.Launcher.utils.ChecksumUtil;
import net.mineguild.Launcher.utils.DownloadUtils;
import net.mineguild.Launcher.utils.ModpackUtils;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.json.JsonFactory;
import net.mineguild.Launcher.utils.json.JsonWriter;
import net.mineguild.Launcher.utils.json.Settings;
import net.mineguild.ModPack.ModPack;
import net.mineguild.ModPack.ModPackInstaller;
import net.mineguild.ModPack.Side;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.gson.GsonBuilder;

public class MineguildLauncher {

  public static File baseDirectory;
  public static boolean doExactCheck = true;
  public static boolean MCRunning;
  public static boolean forceUpdate;
  public static ProcessMonitor procmon;
  public static Console con;
  public static long totalDownloadTime = 0;
  public static Settings settings;

  public static void main(String[] args) throws Exception {
    DownloadUtils.ssl_hack();
    System.setProperty("java.net.preferIPv4Stack", "true");
    ModPack test = new ModPack(System.currentTimeMillis());
    test.setFiles(ChecksumUtil.getFiles(new File("testPack"), FileUtils.listFiles(new File("testPack/mods"), Constants.MODPACK_FILE_FILTER, Constants.MODPACK_DIR_FILTER)));
    JsonWriter.saveModpack(test, new File("new_format.json"));
    Logger.addListener(new StdOutLogger());
    ModPackInstaller.clearFolder(new File("mods"), test, new File("bakup"));
    //List<DownloadInfo> dinfo = ModPackInstaller.checkNeededFiles(new File("modpack"), test, Side.UNIVERSAL);
    //System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(dinfo));
    System.exit(0);
    if (args.length == 1) {
      if (args[0].equals("-updateServer")) {
        MineguildLauncherConsole.update();
        System.exit(0);
      }
    } else if (args.length == 2) {
      if (args[0].equals("-updateServer")) {
        if (args[1].equals("forceUpdate")) {
          forceUpdate = true;
          MineguildLauncherConsole.update();
          System.exit(0);
        }
      }
    }
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

    Logger.addListener(new LogWriter(new File("launcher.log"), LogSource.LAUNCHER));
    con = new Console();
    con.setVisible(true);
    Logger.addListener(con);

    try {
      settings = JsonFactory.loadSettings(new File(OSUtils.getLocalDir(), "settings.json"));
    } catch (IOException e) {
      OSUtils.getLocalDir().mkdirs();
      settings = new Settings(getInstallPath(null));
      JsonWriter.saveSettings(settings, new File(OSUtils.getLocalDir(), "settings.json"));
    }
    if (settings.getModpackPath() == null) {
      settings.setModpackPath(getInstallPath(null));
    }
    AuthWorkDialog dl = new AuthWorkDialog(con);
    dl.start();
    LoginDialog dialog = new LoginDialog(con);
    dialog.run();
    if (!dialog.successfull) {
      Logger.logError("Login not successful... Exiting");
      Thread.sleep(1000);
      System.exit(0);
    }
    JsonWriter.saveSettings(settings, new File(OSUtils.getLocalDir(), "settings.json"));

    addSaveHook();
    LoginResponse res = dialog.response;
    baseDirectory = settings.getModpackPath();
    baseDirectory.mkdirs();

    XModpack m = null;
    boolean updated = true;
    XModpack newest =
        XModpack.fromJson(IOUtils
            .toString(new URL("https://mineguild.net/download/mmp/modpack.json")));
    Logger.logInfo(String.format("Newest pack version: %s released on %s", newest.getVersion(),
        new Date(newest.getReleaseTime()).toString()));
    File curpack = new File(baseDirectory, "version.json");
    forceUpdate = !curpack.exists() || dialog.forceUpdate;
    if (forceUpdate) {
      try {
        ModpackUtils.updateModpack(newest);
      } catch (Exception e) {
        Logger.logError("Modpack update interrupted!", e);
        updated = false;
      }
      m = newest;
    } else {
      try {
        XModpack localPack = XModpack.fromJson(FileUtils.readFileToString(curpack));
        Logger.logInfo(String.format("Local pack version: %s released on %s",
            localPack.getVersion(), localPack.getReleaseDate()));
        if (!newest.getHash().equals(localPack.getHash())) {
          if (newest.isNewer(localPack)) {
            int result =
                JOptionPane.showConfirmDialog(con, String.format(
                    "A new version %s released on %s is available! Do you want to update?",
                    newest.getVersion(), newest.getReleaseDate()), "Update modpack?",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
              Logger.logInfo(String.format("Local: %s [Released: %s] [Hash: %s]",
                  localPack.getVersion(), localPack.getReleaseDate(), localPack.getHash()));
              Logger.logInfo(String.format("Remote: %s [Released: %s] [Hash: %s]",
                  newest.getVersion(), newest.getReleaseDate(), newest.getHash()));
              Logger.logInfo("Updating from Local to Remote");
              try {
                ModpackUtils.updateModpack(localPack, newest);
                m = newest;
              } catch (Exception e) {
                Logger.logError("Modpack update interrupted!", e);
                updated = false;
              }
            } else {
              // We let him launch the pack although he won't be able to play on the server.
              Logger.logInfo("Pack wasn't updated, because user denied.");
            }
          } else {
            Logger.logInfo("Not updating, because not newer");
            m = localPack;
          }
        } else {
          Logger.logInfo("Not updating, because not newer");
          m = localPack;
        }
      } catch (Exception e) {
        Logger.logError("Unable to load local pack, fresh-installing.", e);
        try {
          ModpackUtils.updateModpack(newest);
        } catch (Exception e1) {
          Logger.logError("Modpack update interrupted!", e1);
          updated = false;
        }
        m = newest;
      }
    }
    if (updated) {
      Logger.logInfo("Successfully updated/installed modpack.");
      FileUtils.write(new File(baseDirectory, "version.json"), m.toJson());
      boolean success = true;
      try {
        Logger.logInfo("Preparing MC for launch.");
        MCInstaller.setup(m);
        Logger.logInfo("Downloaded for " + totalDownloadTime / 1000 + " seconds.");
      } catch (Exception e) {
        Logger.logError("Couldn't prepare MC for launch.", e);
        success = false;
      }

      if (success) {
        int result =
            JOptionPane.showConfirmDialog(con,
                "Minecraft (MMP) is ready to launch, do you want to launch it?", "Launch MC?",
                JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.OK_OPTION) {
          Logger.logInfo(String.format("Launching Local: %s [Released: %s] [Hash: %s]",
              m.getVersion(), new Date(m.getReleaseTime()).toString(), m.getHash()));
          MCInstaller.launchMinecraft(m, res);
        } else {
          Logger.logInfo("Not launching Minecraft.");
        }
      } else {
        JOptionPane.showMessageDialog(con, "Something went wrong! Modpack can't be launched now!");
      }
    } else {
      Logger.logInfo("No success installing/updating modpack.");
    }
  }

  public static File getInstallPath(Component par) {
    Component parent = (par == null) ? con : par;
    File folder;
    try {
      folder =
          MineguildLauncher.settings.getModpackPath() == null ? new File("modpack")
              : MineguildLauncher.settings.getModpackPath();
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


}
