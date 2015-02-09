package net.mineguild.Launcher;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import lombok.Getter;
import lombok.Setter;
import net.mineguild.Launcher.log.Console;
import net.mineguild.Launcher.log.LogSource;
import net.mineguild.Launcher.log.LogWriter;
import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.minecraft.LoginResponse;
import net.mineguild.Launcher.minecraft.ProcessMonitor;
import net.mineguild.Launcher.utils.AuthWorkDialog;
import net.mineguild.Launcher.utils.DownloadUtils;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.json.JsonFactory;
import net.mineguild.Launcher.utils.json.JsonWriter;
import net.mineguild.Launcher.utils.json.Settings;
import net.mineguild.ModPack.ModpackRepository;
import net.mineguild.ModPack.Side;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

public class MineguildLauncher {

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
  private static @Getter List<ModpackRepository> repositories = Lists.newArrayList();

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
    /*ModpackRepository repo = JsonFactory.loadRepository(new File("defaultrepository.json"));
    VersionRepository vanilla = new VersionRepository("Vanilla");
    vanilla.setRepoBaseURL("https://mineguild.net/download/mmp/vanilla/");
    ModPack m = new ModPack();
    m.setMinecraftVersion("1.8.1");
    m.setVersion("1.8.1");
    m.setReleaseTime(System.currentTimeMillis());
    vanilla.getVersions().add(m);
    repo.getPacks().put("Vanilla", vanilla);
    JsonWriter.saveModpack(m, new File(m.getHash()));
    JsonWriter.saveRepository(repo, new File("newdefaultrepository.json"));
    System.exit(0);*/
    DownloadUtils.ssl_hack();
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
          
          if (settings.getInstancePath().exists() && settings.getInstancePath().isDirectory()) {
            JOptionPane.showMessageDialog(lFrame,
                "Migrating MMP to new location. This can take a couple of moments.",
                "Migration", JOptionPane.INFORMATION_MESSAGE);
            File newDir = new File(settings.getInstancesPath(), "MMP");
            Logger.logInfo("Migrating old pack.");
            FileUtils.moveDirectory(settings.getInstancePath(), newDir);
          }
          if (settings.getRepositories().isEmpty()) {
            settings.getRepositories().add(Constants.MG_REPOSITORY);
          }
          lFrame.doVersionCheck();
          parent = lFrame;
          if (!getSettings().isFacebookAsked()) {
            int res =
                JOptionPane.showOptionDialog(parent, "Please like us on Facebook if you like us!",
                    "Facebook Like?", JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, lFrame.createImageIcon("/icon.png", "MG Icon"),
                    new String[] {"Yes! (Opens MG FB Page)", "No", "Ask me later"}, "Ask me later");
            if (res == JOptionPane.YES_OPTION) {
              Desktop d = Desktop.getDesktop();
              d.browse(new URI(Constants.MG_FB_PAGE));
              getSettings().setFacebookAsked(true);
            } else if (res == JOptionPane.NO_OPTION) {
              getSettings().setFacebookAsked(true);
            }

          }
          addSaveHook();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
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


}
