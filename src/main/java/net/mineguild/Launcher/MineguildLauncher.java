package net.mineguild.Launcher;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.mineguild.Launcher.log.Console;
import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.minecraft.LoginDialog;
import net.mineguild.Launcher.minecraft.LoginResponse;
import net.mineguild.Launcher.minecraft.MCInstaller;
import net.mineguild.Launcher.minecraft.ProcessMonitor;
import net.mineguild.Launcher.utils.AuthWorkDialog;
import net.mineguild.Launcher.utils.DownloadUtils;
import net.mineguild.Launcher.utils.ModpackUtils;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.json.JSONFactory;
import net.mineguild.Launcher.utils.json.Settings;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class MineguildLauncher {

  public static File baseDirectory;
  public static boolean doExactCheck;
  public static boolean MCRunning;
  public static ProcessMonitor procmon;
  public static Console con;
  public static Settings settings;

  public static void main(String[] args) throws Exception {
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
    DownloadUtils.ssl_hack();
    System.setProperty("java.net.preferIPv4Stack", "true");
    con = new Console();
    con.setVisible(true);
    try {
      settings = JSONFactory.loadSettings(new File(OSUtils.getLocalDir(), "settings.json"));
    } catch (IOException e) {
      OSUtils.getLocalDir().mkdirs();
      settings = new Settings(getInstallPath());
      JSONFactory.saveSettings(settings, new File(OSUtils.getLocalDir(), "settings.json"));
    }
    if (settings.getModpackPath() == null) {
      settings.setModpackPath(getInstallPath());
    }
    AuthWorkDialog dl = new AuthWorkDialog(con);
    dl.start();
    LoginDialog dialog = new LoginDialog(con);
    dialog.run();
    if (!dialog.successfull) {
      Logger.logError("Login not successfull... Exiting");
      Thread.sleep(1000);
      System.exit(0);
    }
    JSONFactory.saveSettings(settings, new File(OSUtils.getLocalDir(), "settings.json"));

    addSaveHook();
    LoginResponse res = dialog.response;
    baseDirectory = settings.getModpackPath();
    baseDirectory.mkdirs();

    Logger.addListener(con);
    Modpack m;
    boolean updated = true;
    Modpack newest =
        Modpack.fromJson(IOUtils
            .toString(new URL("https://mineguild.net/download/mmp/modpack.json")));
    Logger.logInfo(String.format("Newest pack version: %s released on %s", newest.getVersion(),
        new Date(newest.getReleaseTime()).toString()));
    File curpack = new File(baseDirectory, "version.json");
    boolean forceUpdate = !curpack.exists();
    if (args.length == 1) {
      if (args[0].equals("forceupdate")) {
        forceUpdate = true;
      }
    }
    if (forceUpdate) {
      try {
        ModpackUtils.updateModpack(newest);
      } catch (Exception e) {
        Logger.logError("Modpack update interuppted!", e);
        updated = false;
      }
      m = newest;
    } else {
      try {
        Modpack localPack = Modpack.fromJson(FileUtils.readFileToString(curpack));
        Logger.logInfo(String.format("Local pack version: %s released on %s", newest.getVersion(),
            newest.getReleaseDate()));
        if (!newest.getHash().equals(localPack.getHash())) {
          if (newest.isNewer(localPack)) {
            int result =
                JOptionPane.showConfirmDialog(con, String.format(
                    "A new version %s released on %s is available! Do you want to update?",
                    newest.getVersion(), newest.getReleaseDate()));
            if (result == JOptionPane.YES_OPTION) {
              Logger.logInfo(String.format("Local: %s [Released: %s] [Hash: %s]",
                  localPack.getVersion(), localPack.getReleaseDate(), localPack.getHash()));
              Logger.logInfo(String.format("Remote: %s [Released: %s] [Hash: %s]",
                  newest.getVersion(), newest.getReleaseDate(), newest.getHash()));
              Logger.logInfo("Updating from Local to Remote");
              try {
                ModpackUtils.updateModpack(localPack, newest);
              } catch (Exception e) {
                Logger.logError("Modpack update interrupted!", e);
                updated = false;
              }
            } else {
              // We let him launch the pack although he won't be able to play on the server.
              Logger.logInfo("Pack wasn't updated, because user denied.");
            }
            m = newest;
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
        m.setMinecraftVersion("1.8");
        MCInstaller.setup(m);
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

  public static File getInstallPath() {
    int result =
        JOptionPane
            .showConfirmDialog(
                con,
                String
                    .format(
                        "Do you want to select a different install location for the modpack? Otherwise it will be installed in %s",
                        new File("modpack").getAbsolutePath()));
    if (result == JOptionPane.OK_OPTION) {
      JFileChooser chooser = new JFileChooser();
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      result = chooser.showDialog(con, "Use selected directory");
      if (result == JFileChooser.APPROVE_OPTION) {
        return new File(chooser.getSelectedFile(), "/");
      } else {
        return getInstallPath();
      }

    } else {
      return new File("modpack/");
    }
  }

  public static void addSaveHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          JSONFactory.saveSettings(MineguildLauncher.settings, new File(OSUtils.getLocalDir(),
              "settings.json"));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }));
  }


}
