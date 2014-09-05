package net.mineguild.Launcher;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import net.mineguild.Builder.ModpackBuilder;
import net.mineguild.Launcher.log.Console;
import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.minecraft.LoginDialog;
import net.mineguild.Launcher.minecraft.LoginResponse;
import net.mineguild.Launcher.minecraft.MCInstaller;
import net.mineguild.Launcher.minecraft.ProcessMonitor;
import net.mineguild.Launcher.utils.DownloadUtils;
import net.mineguild.Launcher.utils.ModpackUtils;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.json.JSONFactory;
import net.mineguild.Launcher.utils.json.Settings;
import net.mineguild.Launcher.utils.json.versions.OS;

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
    // DownloadDialog d = new DownloadDialog(new HashMap<String, File>(0), "Test");
    // d.setVisible(true); //STUFF...
    try {
      //
      if (OSUtils.getCurrentOS() != net.mineguild.Launcher.utils.OSUtils.OS.WINDOWS)
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      else {
        UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
      }
      /*
       * UIManager.put("nimbusBase", Color.BLACK); UIManager.put("text", Color.WHITE);
       * UIManager.put("nimbusLightBackground", Color.DARK_GRAY); UIManager.put("control",
       * Color.DARK_GRAY); for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) { if
       * ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; }
       * }
       */
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
      settings = new Settings();
      settings.setMCUser("test");
      JSONFactory.saveSettings(settings, new File(OSUtils.getLocalDir(), "settings.json"));
    }
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          JSONFactory.saveSettings(MineguildLauncher.settings, new File(OSUtils.getLocalDir(),
              "settings.json"));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }));
    LoginDialog dialog = new LoginDialog(con);
    dialog.run();
    JSONFactory.saveSettings(settings, new File(OSUtils.getLocalDir(), "settings.json"));
    LoginResponse res = dialog.response;
    baseDirectory = new File("modpack/");
    baseDirectory.mkdirs();

    Logger.addListener(con);
    Modpack m;
    boolean updated = true;
    Modpack newest =
        Modpack.fromJson(IOUtils
            .toString(new URL("https://mineguild.net/download/mmp/modpack.json")));
    Logger.logInfo(String.format("Newest pack version: %s from %s", newest.getVersion(), new Date(
        newest.getReleaseTime()).toString()));
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
        if (!newest.getHash().equals(localPack.getHash())) {
          if (newest.isNewer(localPack)) {
            int result =
                JOptionPane.showConfirmDialog(con, String.format(
                    "A new version %s[Released: %s] is available! Do you want to update?",
                    newest.getVersion(), new Date(newest.getReleaseTime()).toString()));
            if (result == JOptionPane.YES_OPTION) {
              Logger.logInfo(String.format(
                  "Updating from %s[ReleaseTime:%s] to %s[ReleaseTime:%s]", localPack.getVersion(),
                  new Date(localPack.getReleaseTime()).toString(), newest.getVersion(), new Date(
                      newest.getReleaseTime()).toString()));
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
        MCInstaller.setup(m);
      } catch (Exception e) {
        success = false;
      }

      if (success) {
        MCInstaller.launchMinecraft(m, res);
      } else {
        JOptionPane.showMessageDialog(null, "Something went wrong! Modpack can't be launched now!");
      }
    } else {
      Logger.logInfo("No success installing/updating modpack.");
    }
  }
}
