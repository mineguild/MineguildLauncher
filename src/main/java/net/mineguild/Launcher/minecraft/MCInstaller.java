package net.mineguild.Launcher.minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

import net.mineguild.Builder.ModpackBuilder;
import net.mineguild.Launcher.Constants;
import net.mineguild.Launcher.MineguildLauncher;
import net.mineguild.Launcher.download.DownloadInfo;
import net.mineguild.Launcher.download.MultithreadedDownloadDialog;
import net.mineguild.Launcher.log.LogEntry;
import net.mineguild.Launcher.log.LogLevel;
import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.log.StreamLogger;
import net.mineguild.Launcher.utils.ChecksumUtil;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.OSUtils.OS;
import net.mineguild.Launcher.utils.Parallel;
import net.mineguild.Launcher.utils.json.JsonFactory;
import net.mineguild.Launcher.utils.json.Settings.JavaSettings;
import net.mineguild.Launcher.utils.json.assets.AssetIndex;
import net.mineguild.Launcher.utils.json.assets.AssetIndex.Asset;
import net.mineguild.Launcher.utils.json.versions.Library;
import net.mineguild.Launcher.utils.json.versions.Version;
import net.mineguild.Launcher.utils.winreg.JavaFinder;
import net.mineguild.Launcher.utils.winreg.JavaInfo;
import net.mineguild.ModPack.ModPack;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

public class MCInstaller {

  private static String packmcversion = new String();
  private static String packbasejson = new String();
  private static File launchPath;
  private static File gameDirectory;
  private static long totalAssetSize = 0;

  public static void setup(final ModPack pack, File launchPath, File gameDirectory,
      JavaSettings javaSettings, LoginResponse resp, boolean doLaunch) throws Exception {
    List<DownloadInfo> libraries = Lists.newArrayList();
    List<DownloadInfo> assets = Lists.newArrayList();
    MCInstaller.launchPath = launchPath;
    MCInstaller.gameDirectory = gameDirectory;
    packmcversion = pack.getMinecraftVersion();
    packbasejson = new String();
    try {
      libraries = getLibraries(pack);
    } catch (Exception e) {
      Logger.logError("Error getting libs", e);
    }
    try {
      assets = getAssets();
    } catch (Exception e) {
      Logger.logError("Error getting assets", e);
    }
    MultithreadedDownloadDialog dlDialog;
    if (assets.size() > 0) {
      long startTime = System.currentTimeMillis();
      dlDialog =
          new MultithreadedDownloadDialog(MineguildLauncher.getParent(), assets,
              "Downloading Assets", totalAssetSize);
      dlDialog.setVisible(true);
      if (!dlDialog.run()) {
        dlDialog.dispose();
        throw new Exception("Download was interrupted!");
      }
      dlDialog.dispose();
      MineguildLauncher.totalDownloadTime += System.currentTimeMillis() - startTime;
    }
    if (libraries.size() > 0) {
      long startTime = System.currentTimeMillis();
      dlDialog =
          new MultithreadedDownloadDialog(MineguildLauncher.getParent(), libraries,
              "Downloading Libraries");
      dlDialog.setVisible(true);
      if (!dlDialog.run()) {
        dlDialog.dispose();
        throw new Exception("Download was interrupted!");
      }
      dlDialog.dispose();
      MineguildLauncher.totalDownloadTime += System.currentTimeMillis() - startTime;
    }
    if (libraries.size() + assets.size() > 0) {
      JOptionPane.showMessageDialog(MineguildLauncher.getParent(), libraries.size() + assets.size()
          + " file(s) successfully downloaded!");
    }
    if (doLaunch) {
      launchMinecraft(pack, resp, javaSettings);
    }
  }

  private static List<DownloadInfo> getLibraries(ModPack pack) throws Exception {
    List<DownloadInfo> list = Lists.newArrayList();
    File forgeJson = new File(gameDirectory, "pack.json");
    Version forgeVersion = null;
    if (forgeJson.exists()) {
      try {
        Version testVersion = JsonFactory.loadVersion(forgeJson);
        String[] split = testVersion.id.split("-");
        if ((split[split.length - 1]).equals(pack.getForgeVersion().split("-")[1])) {
          forgeVersion = testVersion;
        } else {
          Logger.logInfo("Local lib-file is out of date - download new file");
        }
      } catch (Exception e) {
        Logger.logError("Couldn't load local pack file... Getting online file");
      }
    }
    if (forgeVersion == null && !pack.getForgeVersion().isEmpty()) {
      FileUtils.copyURLToFile(
          new URL(Constants.MG_FORGE + pack.getForgeVersion() + "/version.json"), forgeJson);
      forgeVersion = JsonFactory.loadVersion(forgeJson);
    }

    File local;
    File libDir = new File(launchPath, "libraries");

    if (forgeVersion != null) {
      if (forgeVersion.jar != null && !forgeVersion.jar.isEmpty())
        packmcversion = forgeVersion.jar;
      if (forgeVersion.inheritsFrom != null && !forgeVersion.inheritsFrom.isEmpty())
        packbasejson = forgeVersion.inheritsFrom;

      for (Library lib : forgeVersion.getLibraries()) {
        local = new File(libDir, lib.getPath());
        if (!local.exists() || MineguildLauncher.forceUpdate) {
          if (lib.checksums != null) {
            list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local,
                local.getName(), lib.checksums, "sha1", DownloadInfo.DLType.NONE,
                DownloadInfo.DLType.NONE));
          } else if (lib.url != null) {
            if (lib.url.toLowerCase().equals(Constants.MG_LIBS)) {
              list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local, local
                  .getName(), null, "md5", DownloadInfo.DLType.ContentMD5,
                  DownloadInfo.DLType.ContentMD5));
            }
          } else {
            list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local, local.getName()));
          }
        }
      }

    }

    if (packbasejson == null || packbasejson.isEmpty())
      packbasejson = packmcversion;
    URL url =
        new URL(Constants.MC_DL
            + "versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", packbasejson));
    File json =
        new File(launchPath, "versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", packbasejson));
    FileUtils.copyURLToFile(url, json);

    Version mcJson = JsonFactory.loadVersion(json);

    for (Library lib : mcJson.getLibraries()) {
      if (lib.natives == null) {
        local = new File(libDir, lib.getPath());
        if (!local.exists() || MineguildLauncher.forceUpdate) {
          list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local, lib.getPath()));
        }
      } else {
        local = new File(libDir, lib.getPathNatives());
        if (!local.exists() || MineguildLauncher.forceUpdate) {
          list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPathNatives()), local, lib
              .getPathNatives()));
        }
      }
    }
    local =
        new File(launchPath + "/versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}", packbasejson));
    if (!local.exists()) {
      list.add(new DownloadInfo(new URL(Constants.MC_DL
          + "versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}", packbasejson)), local, local
          .getName()));
    }
    return list;
  }

  private static List<DownloadInfo> getAssets() throws Exception {
    List<DownloadInfo> list = Lists.newArrayList();
    File forgeJson = new File(gameDirectory, "pack.json");
    Version version = null;
    if(forgeJson.exists()){
    version = JsonFactory.loadVersion(forgeJson);
    } else {
      version = JsonFactory.loadVersion(new File(launchPath, "versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", packbasejson)));
    }


    File json =
        new File(launchPath, "assets/indexes/{MC_VER}.json".replace("{MC_VER}", version.getAssets()));
    if (MineguildLauncher.forceUpdate || !json.exists()) {
      FileUtils.copyURLToFile(
          new URL("https://s3.amazonaws.com/Minecraft.Download/indexes/${version}.json".replace(
              "${version}", version.getAssets())), json);
    }

    AssetIndex index = JsonFactory.loadAssetIndex(json);

    Collection<DownloadInfo> tmp;
    Logger.logInfo("Starting asset hash checking... Please wait...");
    long start = System.currentTimeMillis();
    Parallel.TaskHandler<DownloadInfo> th =
        new Parallel.ForEach<Entry<String, Asset>, DownloadInfo>(index.objects.entrySet())
            .withFixedThreads(2 * OSUtils.getNumCores())
            // .configurePoolSize(2*2*OSUtils.getNumCores(), 10)
            .apply(new Parallel.F<Map.Entry<String, AssetIndex.Asset>, DownloadInfo>() {
              public DownloadInfo apply(Map.Entry<String, AssetIndex.Asset> e) {
                try {
                  String name = e.getKey();
                  AssetIndex.Asset asset = e.getValue();
                  String path = asset.hash.substring(0, 2) + "/" + asset.hash;
                  final File local = new File(launchPath, "assets/objects/" + path);
                  if (local.exists() && !asset.hash.equals(ChecksumUtil.getSHA(local))) {
                    local.delete();
                  }
                  if (!local.exists()) {
                    totalAssetSize += asset.size;
                    return (new DownloadInfo(new URL(Constants.MC_RES + path), local, name, Lists
                        .newArrayList(asset.hash), "sha1"));
                  }
                } catch (Exception ex) {
                  Logger.logError("Asset hash check failed", ex);
                }
                // values() will drop null entries
                return null;
              }
            });
    tmp = th.values();
    Logger.logInfo(String.format("Finished asset hash checking in %d seconds.",
        (System.currentTimeMillis() - start) / 1000));
    list.addAll(tmp);
    // kill executorservice
    th.shutdown();

    return list;
  }

  private static void launchMinecraft(ModPack pack, LoginResponse resp, JavaSettings set) {
    try {
      File minecraftDir = launchPath;
      File instancePath = gameDirectory;
      String gameFolder = gameDirectory.getAbsolutePath();
      File assetDir = new File(minecraftDir, "assets");
      File libDir = new File(minecraftDir, "libraries");
      File natDir = new File(minecraftDir, "natives");
      Logger.logInfo("Setting up native libraries for MMP v " + pack.getVersion() + " MC "
          + packmcversion);
      if (!gameDirectory.exists())
        gameDirectory.mkdirs();

      if (natDir.exists()) {
        natDir.delete();
      }
      natDir.mkdirs();
      Version base =
          JsonFactory.loadVersion(new File(minecraftDir, "/versions/{MC_VER}/{MC_VER}.json"
              .replace("{MC_VER}", packbasejson)));
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
      if (new File(instancePath, "pack.json").exists()) {
        packjson = JsonFactory.loadVersion(new File(instancePath, "pack.json"));
        for (Library lib : packjson.getLibraries()) {
          Logger.logError(new File(libDir, lib.getPath()).getAbsolutePath());
          classpath.add(new File(libDir, lib.getPath()));
        }
      } else {
        packjson = base;
      }
      classpath.add(new File(minecraftDir, "/versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}",
          packmcversion)));
      for (Library lib : base.getLibraries()) {
        classpath.add(new File(libDir, lib.getPath()));
      }

      Process minecraftProcess =
          MCLauncher.launchMinecraft(set.getJavaPath(), gameFolder, assetDir, natDir, classpath,
              packjson.mainClass != null ? packjson.mainClass : base.mainClass,
              packjson.minecraftArguments != null ? packjson.minecraftArguments
                  : base.minecraftArguments,
              packjson.assets != null ? packjson.assets : base.getAssets(), pack
                  .getMinecraftVersion(), resp.getAuth(), false, set);

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
            if (ModpackBuilder.launch != null) {
              ModpackBuilder.launch.mcStopped();
            } else if (MineguildLauncher.getLFrame() != null) {
              MineguildLauncher.getLFrame().mcStopped();
            }
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
