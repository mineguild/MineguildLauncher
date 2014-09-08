package net.mineguild.Launcher.minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

import net.mineguild.Launcher.Constants;
import net.mineguild.Launcher.MineguildLauncher;
import net.mineguild.Launcher.Modpack;
import net.mineguild.Launcher.download.DownloadDialog;
import net.mineguild.Launcher.download.DownloadInfo;
import net.mineguild.Launcher.download.MultithreadDownloadDialog;
import net.mineguild.Launcher.log.LogEntry;
import net.mineguild.Launcher.log.LogLevel;
import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.log.StreamLogger;
import net.mineguild.Launcher.utils.ChecksumUtil;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.OSUtils.OS;
import net.mineguild.Launcher.utils.Parallel;
import net.mineguild.Launcher.utils.json.JSONFactory;
import net.mineguild.Launcher.utils.json.assets.AssetIndex;
import net.mineguild.Launcher.utils.json.versions.Library;
import net.mineguild.Launcher.utils.json.versions.Version;
import net.mineguild.Launcher.utils.winreg.JavaFinder;
import net.mineguild.Launcher.utils.winreg.JavaInfo;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

public class MCInstaller {

  private static String packmcversion = new String();
  private static String packbasejson = new String();
  private static long totalAssetSize = 0;

  public static void setup(final Modpack pack) throws Exception {
    List<DownloadInfo> libraries = null;
    List<DownloadInfo> assets = null;
    packmcversion = pack.getMinecraftVersion();
    try {
      libraries = getLibraries(pack);
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      assets = getAssets();
    } catch (Exception e) {
      e.printStackTrace();
    }
    MultithreadDownloadDialog dlDialog;
    if (assets.size() > 0) {
      dlDialog = new MultithreadDownloadDialog(assets, "Downloading Assets", totalAssetSize);
      dlDialog.setVisible(true);
      if (!dlDialog.start()) {
        dlDialog.dispose();
        throw new Exception("Download was interrupted!");
      }
      dlDialog.dispose();
    }
    if (libraries.size() > 0) {
      dlDialog = new MultithreadDownloadDialog(libraries, "Downloading Libraries");
      dlDialog.setVisible(true);
      if (!dlDialog.start()) {
        dlDialog.dispose();
        throw new Exception("Download was interrupted!");
      }
      dlDialog.dispose();
    }
    if (libraries.size() + assets.size() > 0) {
      JOptionPane.showMessageDialog(null, libraries.size() + assets.size()
          + " file(s) successfully downloaded!");
    }
  }

  private static List<DownloadInfo> getLibraries(Modpack pack) throws Exception {
    List<DownloadInfo> list = Lists.newArrayList();
    File forgeJson = new File(MineguildLauncher.baseDirectory, "pack.json");
    FileUtils.copyURLToFile(new URL(Constants.MG_FORGE + pack.getForgeVersion() + "/version.json"),
        forgeJson);

    File local;
    File libDir = new File(MineguildLauncher.baseDirectory, "libraries");

    Version forgeVersion = JSONFactory.loadVersion(forgeJson);
    if (forgeVersion.jar != null && !forgeVersion.jar.isEmpty())
      packmcversion = forgeVersion.jar;
    if (forgeVersion.inheritsFrom != null && !forgeVersion.inheritsFrom.isEmpty())
      packbasejson = forgeVersion.inheritsFrom;

    for (Library lib : forgeVersion.getLibraries()) {
      if (lib.natives == null) {
        local = new File(libDir, lib.getPath());
        if (!local.exists()) {
          if (lib.checksums != null) {
            list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local,
                local.getName(), lib.checksums, "sha1", DownloadInfo.DLType.NONE,
                DownloadInfo.DLType.NONE));
          } else if (lib.download != null && lib.download) {
            list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local, local.getName()));
          } else {
            list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local, local.getName()));
          }
        }
      } else {
        local = new File(libDir, lib.getPathNatives());
        if (lib.checksums != null) {
          list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPathNatives()), local, local
              .getName(), lib.checksums, "sha1", DownloadInfo.DLType.NONE, DownloadInfo.DLType.NONE));
        } else if (lib.download != null && lib.download) {
          list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPathNatives()), local, local
              .getName()));
        } else {
          list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPathNatives()), local, local
              .getName()));
        }
      }
    }

    if (packbasejson == null || packbasejson.isEmpty())
      packbasejson = packmcversion;
    URL url =
        new URL(Constants.MC_DL
            + "versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", packbasejson));
    File json =
        new File(MineguildLauncher.baseDirectory, "versions/{MC_VER}/{MC_VER}.json".replace(
            "{MC_VER}", packbasejson));
    FileUtils.copyURLToFile(url, json);

    Version mcJson = JSONFactory.loadVersion(json);

    for (Library lib : mcJson.getLibraries()) {
      if (lib.natives == null) {
        local = new File(libDir, lib.getPath());
        if (!local.exists()) {
          list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local, lib.getPath()));
        }
      } else {
        local = new File(libDir, lib.getPathNatives());
        if (!local.exists()) {
          list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPathNatives()), local, lib
              .getPathNatives()));
        }
      }
    }
    local =
        new File(MineguildLauncher.baseDirectory
            + "/versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}", packbasejson));
    if (!local.exists()) {
      list.add(new DownloadInfo(new URL(Constants.MC_DL
          + "versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}", packbasejson)), local, local
          .getName()));
    }
    return list;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static List<DownloadInfo> getAssets() throws Exception {
    List<DownloadInfo> list = Lists.newArrayList();
    File forgeJson = new File(MineguildLauncher.baseDirectory, "pack.json");
    Version version = JSONFactory.loadVersion(forgeJson);

    File json =
        new File(MineguildLauncher.baseDirectory, "assets/indexes/{MC_VER}.json".replace(
            "{MC_VER}", packbasejson));
    FileUtils.copyURLToFile(
        new URL("https://s3.amazonaws.com/Minecraft.Download/indexes/${version}.json".replace(
            "${version}", version.getAssets())), json);

    AssetIndex index = JSONFactory.loadAssetIndex(json);

    Collection<DownloadInfo> tmp;
    Parallel.TaskHandler th =
        new Parallel.ForEach(index.objects.entrySet()).withFixedThreads(2 * OSUtils.getNumCores())
        // .configurePoolSize(2*2*OSUtils.getNumCores(), 10)
            .apply(new Parallel.F<Map.Entry<String, AssetIndex.Asset>, DownloadInfo>() {
              public DownloadInfo apply(Map.Entry<String, AssetIndex.Asset> e) {
                try {
                  String name = e.getKey();
                  AssetIndex.Asset asset = e.getValue();
                  String path = asset.hash.substring(0, 2) + "/" + asset.hash;
                  final File local =
                      new File(MineguildLauncher.baseDirectory, "assets/objects/" + path);
                  if (local.exists() && !asset.hash.equals(ChecksumUtil.getSHA(local))) {
                    local.delete();
                  }
                  if (!local.exists()) {
                    totalAssetSize += asset.size;
                    return (new DownloadInfo(new URL(Constants.MC_RES + path), local, name, Lists
                        .newArrayList(asset.hash), "sha1"));
                  }
                } catch (Exception ex) {
                  // Logger.logError("Asset hash check failed", ex);
                }
                // values() will drop null entries
                return null;
              }
            });
    tmp = th.values();
    list.addAll(tmp);
    // kill executorservice
    th.shutdown();

    return list;
  }

  public static void launchMinecraft(Modpack pack, LoginResponse resp) {
    try {
      File packDir = MineguildLauncher.baseDirectory;
      File gameDir = new File(packDir, "minecraft");
      String gameFolder = gameDir.getAbsolutePath();
      File assetDir = new File(packDir, "assets");
      File libDir = new File(packDir, "libraries");
      File natDir = new File(packDir, "natives");
      // Logger.logInfo("Setting up native libraries for " + pack.getName() + " v " + packVer +
      // " MC " + packmcversion);
      if (!gameDir.exists())
        gameDir.mkdirs();

      if (natDir.exists()) {
        natDir.delete();
      }
      natDir.mkdirs();
      Version base =
          JSONFactory.loadVersion(new File(packDir, "/versions/{MC_VER}/{MC_VER}.json".replace(
              "{MC_VER}", packbasejson)));
      byte[] buf = new byte[1024];
      for (Library lib : base.getLibraries()) {
        if (lib.natives != null) {
          File local = new File(libDir, lib.getPathNatives());
          ZipInputStream input = null;
          try {
            input = new ZipInputStream(new FileInputStream(local));
            ZipEntry entry = input.getNextEntry();
            while (entry != null) {
              String name = entry.getName();
              int n;
              if (lib.extract == null || !lib.extract.exclude(name)) {
                File output = new File(natDir, name);
                output.getParentFile().mkdirs();
                FileOutputStream out = new FileOutputStream(output);
                while ((n = input.read(buf, 0, 1024)) > -1) {
                  out.write(buf, 0, n);
                }
                out.close();
              }
              input.closeEntry();
              entry = input.getNextEntry();
            }
          } catch (Exception e) {
            // ErrorUtils.tossError("Error extracting native libraries");
            Logger.logError("Error extracting natives", e);
          } finally {
            try {
              input.close();
            } catch (IOException e) {
            }
          }
        }
      }
      List<File> classpath = Lists.newArrayList();
      Version packjson = new Version();
      if (new File(packDir, "pack.json").exists()) {
        packjson = JSONFactory.loadVersion(new File(packDir, "pack.json"));
        for (Library lib : packjson.getLibraries()) {
          Logger.logError(new File(libDir, lib.getPath()).getAbsolutePath());
          classpath.add(new File(libDir, lib.getPath()));
        }
        // }
      } else {
        packjson = base;
      }
      classpath.add(new File(packDir, "/versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}",
          packmcversion)));
      for (Library lib : base.getLibraries()) {
        classpath.add(new File(libDir, lib.getPath()));
      }


      Process minecraftProcess =
          MCLauncher.launchMinecraft(getDefaultJavaPath(), gameFolder, assetDir, natDir, classpath,
              packjson.mainClass != null ? packjson.mainClass : base.mainClass,
              packjson.minecraftArguments != null ? packjson.minecraftArguments
                  : base.minecraftArguments,
              packjson.assets != null ? packjson.assets : base.getAssets(), "2048", "256m", pack
                  .getMinecraftVersion(), resp.getAuth(), false);

      MineguildLauncher.MCRunning = true;
      MineguildLauncher.con.minecraftStarted();

      StreamLogger.prepare(minecraftProcess.getInputStream(),
          new LogEntry().level(LogLevel.UNKNOWN));

      String[] ignore = {"Session ID is token"};
      StreamLogger.setIgnore(ignore);
      StreamLogger.doStart();
      // String curVersion =
      // (Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") ?
      // pack.getVersion() : Settings.getSettings().getPackVer()).replace(".", "_");
      // TrackerUtils.sendPageView(ModPack.getSelectedPack().getName(), "Launched / " +
      // ModPack.getSelectedPack().getName() + " / " + curVersion.replace('_', '.'));
      try {
        Thread.sleep(1500);
      } catch (InterruptedException e) {
      }
      try {
        minecraftProcess.exitValue();
      } catch (IllegalThreadStateException e) {

        // LaunchFrame.getInstance().setVisible(false);
        MineguildLauncher.procmon = ProcessMonitor.create(minecraftProcess, new Runnable() {

          @Override
          public void run() {
            if (MineguildLauncher.con != null)
              MineguildLauncher.con.minecraftStopped();
            // LaunchFrame launchFrame = LaunchFrame.getInstance(); launchFrame.setVisible(true);
            /*
             * Main.getEventBus().post(new EnableObjectsEvent()); try {
             * Settings.getSettings().load(new
             * FileInputStream(Settings.getSettings().getConfigFile()));
             * LaunchFrame.getInstance().tabbedPane.remove(1); LaunchFrame.getInstance().optionsPane
             * = new OptionsPane(Settings.getSettings());
             * LaunchFrame.getInstance().tabbedPane.add(LaunchFrame.getInstance().optionsPane, 1);
             * LaunchFrame.getInstance().tabbedPane.setIconAt(1,
             * LauncherStyle.getCurrentStyle().filterHeaderIcon
             * (this.getClass().getResource("/image/tabs/options.png"))); } catch (Exception e1) {
             * Logger.logError("Failed to reload settings after launcher closed", e1); } }
             */
            MineguildLauncher.MCRunning = false;
          }
        });

      }
    } catch (Exception e) {
      Logger.logError("Error while running launchMinecraft()", e);
      e.printStackTrace();
    }
  }

  public static String getDefaultJavaPath() {
    JavaInfo javaVersion;

    if (OSUtils.getCurrentOS() == OS.MACOSX) {
      javaVersion = JavaFinder.parseJavaVersion();

      if (javaVersion != null && javaVersion.path != null)
        return javaVersion.path;
    } else if (OSUtils.getCurrentOS() == OS.WINDOWS) {
      javaVersion = JavaFinder.parseJavaVersion();

      if (javaVersion != null && javaVersion.path != null)
        return javaVersion.path.replace(".exe", "w.exe");
    }
    // Windows specific code adds <java.home>/bin/java no need mangle javaw.exe here.
    return System.getProperty("java.home") + "/bin/java";
  }



}
