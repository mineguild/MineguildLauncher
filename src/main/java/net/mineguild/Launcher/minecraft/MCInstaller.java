package net.mineguild.Launcher.minecraft;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import net.mineguild.Launcher.Constants;
import net.mineguild.Launcher.MineguildLauncher;
import net.mineguild.Launcher.Modpack;
import net.mineguild.Launcher.download.DownloadDialog;
import net.mineguild.Launcher.download.DownloadInfo;
import net.mineguild.Launcher.utils.ChecksumUtil;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.Parallel;
import net.mineguild.Launcher.utils.json.JSONFactory;
import net.mineguild.Launcher.utils.json.assets.AssetIndex;
import net.mineguild.Launcher.utils.json.versions.Library;
import net.mineguild.Launcher.utils.json.versions.Version;

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
    DownloadDialog dlDialog;
    if (assets.size() > 0) {
      dlDialog = new DownloadDialog(assets, "Downloading Assets", totalAssetSize);
      dlDialog.setVisible(true);
      if (!dlDialog.start()) {
        dlDialog.dispose();
        throw new Exception("Download was interrupted!");
      }
      dlDialog.dispose();
    }
    if (libraries.size() > 0) {
      dlDialog = new DownloadDialog(libraries, "Downloading Libraries");
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
      local = new File(libDir, lib.getPath());
      if (!local.exists()) {
        if (lib.checksums != null) {
          list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local, local.getName(),
              lib.checksums, "sha1", DownloadInfo.DLType.NONE, DownloadInfo.DLType.NONE));
        } else if (lib.download != null && lib.download) {
          list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local, lib.getPath()));
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
          list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local, lib.getPath(),
              true));
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
}
