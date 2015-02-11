package net.mineguild.Launcher;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Frame;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.ProgressMonitorInputStream;
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
import net.mineguild.Launcher.utils.OSUtils.OS;
import net.mineguild.Launcher.utils.json.JsonFactory;
import net.mineguild.Launcher.utils.json.JsonWriter;
import net.mineguild.Launcher.utils.json.Settings;
import net.mineguild.ModPack.ModpackRepository;
import net.mineguild.ModPack.Side;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

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
    /*
     * ModpackRepository repo = JsonFactory.loadRepository(new File("defaultrepository.json"));
     * VersionRepository vanilla = repo.getPacks().get("ForgeVanilla");
     * vanilla.setRepoBaseURL("https://mineguild.net/download/mmp/vanilla_forge/"); ModPack m = new
     * ModPack(); m.setMinecraftVersion("1.8"); m.setVersion("1.8");
     * m.setForgeVersion("1.8-11.14.0.1299"); // ModPackVersion newer = (ModPackVersion)
     * vanilla.getVersions().toArray()[0]; m.setReleaseTime(System.currentTimeMillis());
     * vanilla.getVersions().add(m); repo.getPacks().put("ForgeVanilla", vanilla);
     * JsonWriter.saveModpack(m, new File(m.getHash())); JsonWriter.saveRepository(repo, new
     * File("defaultrepository.json")); System.exit(0);
     */
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

          if (settings.getInstancePath() != null) {
            if (settings.getInstancePath().exists() && settings.getInstancePath().isDirectory()) {
              JOptionPane.showMessageDialog(lFrame,
                  "Migrating MMP to new location. This can take a couple of moments.", "Migration",
                  JOptionPane.INFORMATION_MESSAGE);
              File newDir = new File(settings.getInstancesPath(), "MMP");
              Logger.logInfo("Migrating old pack.");
              FileUtils.moveDirectory(settings.getInstancePath(), newDir);
              settings.setLastPack("MMP");
            }
          }
          Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
              if (OSUtils.getCurrentOS() == OS.WINDOWS) {
                try {
                  if (!new File(OSUtils.getLocalDir(), "java").exists()) {
                    File java = new File(OSUtils.getLocalDir(), "java.zip");
                    URL javaUrl = null;
                    boolean corruptZip = false;
                    if (OSUtils.is64BitWindows()) {
                      javaUrl = new URL("https://mineguild.net/download/mmp/java64.zip");
                    } else {
                      javaUrl = new URL("https://mineguild.net/download/mmp/java.zip");
                    }

                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(java));
                    URLConnection con = javaUrl.openConnection();
                    InputStream is = con.getInputStream();
                    ProgressMonitorInputStream input =
                        new ProgressMonitorInputStream(lFrame, "Downloading Java", is);

                    ProgressMonitor monitor = input.getProgressMonitor();
                    monitor.setMaximum(con.getContentLength());
                    monitor.setMillisToPopup(0);
                    monitor.setMillisToDecideToPopup(0);
                    // do some configuration for monitor here
                    try {
                      int ch;
                      do {
                        try {
                          ch = input.read();
                          // note: writing also the last -1 value
                          out.write(ch);
                        } catch (Exception e) {
                          corruptZip = true;
                          break;
                        }
                      } while (ch != -1);
                    } catch (Exception e) {
                    } finally {
                      input.close();
                      out.close();
                    }

                    if (!corruptZip) {
                      ZipFile zipFile = new ZipFile(java);
                      Enumeration<? extends ZipEntry> entries = zipFile.entries();
                      while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        File entryDestination =
                            new File(new File(OSUtils.getLocalDir(), "java"), entry.getName());
                        entryDestination.getParentFile().mkdirs();
                        if (entry.isDirectory())
                          entryDestination.mkdirs();
                        else {
                          InputStream in = zipFile.getInputStream(entry);
                          OutputStream outS = new FileOutputStream(entryDestination);
                          IOUtils.copy(in, outS);
                          IOUtils.closeQuietly(in);
                          IOUtils.closeQuietly(outS);
                        }
                      }
                      zipFile.close();
                      settings.getJavaSettings().setJavaPath(
                          new File(OSUtils.getLocalDir(), "java/bin/javaw.exe").getAbsolutePath());
                      lFrame.loadSettings();
                    }

                  }
                } catch (Exception e) {
                  Logger.logError("Error while installing java", e);
                }
              }
            }
          });
          t.start();

          if (settings.getRepositories().isEmpty()) {
            settings.getRepositories().add(Constants.MG_REPOSITORY);
            settings.setLastPack("MMP");
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
