package net.mineguild.Launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.mineguild.Launcher.download.AssetDownloader;
import net.mineguild.Launcher.download.DownloadInfo;
import net.mineguild.Launcher.download.DownloadInfo.DLType;
import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.log.StdOutLogger;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.json.JsonFactory;
import net.mineguild.Launcher.utils.json.JsonWriter;
import net.mineguild.ModPack.ModPack;
import net.mineguild.ModPack.ModPackInstaller;
import net.mineguild.ModPack.Side;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

public class MineguildLauncherConsole {

  public static boolean forceUpdate;

  public static File baseDir;
  public static final int BUFFER_SIZE = 8192;
  public static int currentFile = 0;
  public static boolean allDownloaded = true;
  public static int amountOfFiles = 0;
  public static double start = 0;
  public static double speed = 0;
  public static long totalBytesRead = 0;
  public static Side side;

  public static void update(Side side) throws Exception {
    MineguildLauncherConsole.side = side;
    Logger.addListener(new StdOutLogger());
    File newestFile = new File(".newest.json");
    FileUtils.copyURLToFile(new URL(Constants.MG_MMP + "modpack.json"), newestFile);
    ModPack newest = JsonFactory.loadModpack(newestFile);
    baseDir = new File(".");
    Logger.logInfo(String.format("Newest pack version: %s released on %s", newest.getVersion(),
        newest.getReleaseDate()));
    File curpack = new File("version.json");
    forceUpdate = MineguildLauncher.forceUpdate || !curpack.exists();
    if (forceUpdate)
      forceUpdate(newest);
    else {
      try {
        ModPack localPack = JsonFactory.loadModpack(new File("version.json"));
        Logger.logInfo(String.format("Local pack version: %s released on %s", newest.getVersion(),
            newest.getReleaseDate()));
        if (newest.isNewer(localPack)) {
          Logger.logInfo(String.format("Local: %s [Released: %s] [Hash: %s]",
              localPack.getVersion(), localPack.getReleaseDate(), localPack.getHash()));
          Logger.logInfo(String.format("Remote: %s [Released: %s] [Hash: %s]", newest.getVersion(),
              newest.getReleaseDate(), newest.getHash()));
          Logger.logInfo("Updating from Local to Remote");
          try {
            updateModpack(localPack, newest);
          } catch (Exception e) {
            Logger.logError("Can't update modpack!", e);
          }
        }
      } catch (Exception e) {
        try {
          forceUpdate(newest);
        } catch (Throwable t) {
          Logger.logError("Can't update modpack!", t);
        }
      }
    }

  }

  public static void forceUpdate(ModPack newPack) throws Exception {
    updateModpack(null, newPack);
  }

  public static void updateModpack(ModPack currentPack, ModPack newPack) throws Exception {
    Logger.logInfo("Moving untracked mods to modsBackup");
    File modsBackup = new File(baseDir, "modsBackup");
    modsBackup.mkdirs();
    FileUtils.cleanDirectory(modsBackup);

    if (new File(baseDir, "mods").exists()) {
      ModPackInstaller.clearFolder(new File(baseDir, "mods"), newPack, side, modsBackup);
    }
    List<DownloadInfo> downloads = ModPackInstaller.checkNeededFiles(baseDir, newPack, side);
    amountOfFiles = downloads.size();
    start = System.nanoTime();
    ExecutorService executor = Executors.newFixedThreadPool(OSUtils.getNumCores() * 2);
    for (DownloadInfo download : downloads) {
      try {
        Runnable worker = new FileDownloader(download);
        executor.execute(worker);
      } catch (Exception ignored) {
      }

    }
    executor.shutdown();
    try {
      executor.awaitTermination(10 * downloads.size(), TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }

    if (allDownloaded) {
      Logger.logInfo("Update successfull!");
      JsonWriter.saveModpack(newPack, new File("version.json"));
    } else {
      Logger.logError("Update unsuccessfull!");
    }
  }


  public static class FileDownloader implements Runnable {

    public final DownloadInfo asset;

    public FileDownloader(DownloadInfo asset) {
      this.asset = asset;
    }


    @Override
    public void run() {
      byte[] buffer = new byte[BUFFER_SIZE];
      boolean downloadSuccess = false;
      List<String> remoteHash = asset.hash;
      int attempt = 0;
      final int attempts = 5;
      while (!downloadSuccess && (attempt < attempts)) {
        try {
          if (remoteHash == null) {
            remoteHash = Lists.newArrayList();
          }
          if (attempt++ > 0) {
            Logger
                .logInfo("Connecting.. Try " + attempt + " of " + attempts + " for: " + asset.url);
          }

          // Will this break something?
          // HTTPURLConnection con = (HttpURLConnection) asset.url.openConnection();
          URLConnection con = asset.url.openConnection();
          if (con instanceof HttpURLConnection) {
            con.setRequestProperty("Cache-Control", "no-cache, no-transform");
            if (asset.url.toString().contains(Constants.MG_GET_SCRIPT)) {
              ((HttpURLConnection) con).setRequestMethod("GET");
            } else {
              ((HttpURLConnection) con).setRequestMethod("HEAD");
            }
            con.connect();
          }

          // gather data for basic checks
          long remoteSize = Long.parseLong(con.getHeaderField("Content-Length"));
          if (remoteSize == 0) {
            downloadSuccess = true;
            continue;
          }

          if (asset.hash == null && asset.getPrimaryDLType() == DLType.ETag) {
            String eTag = con.getHeaderField("ETag").replace("\"", "");
            remoteHash.clear();
            remoteHash.add(eTag);
          }

          if (asset.hash == null && asset.getPrimaryDLType() == DLType.ContentMD5) {
            remoteHash.clear();
            remoteHash.add(con.getHeaderField("Content-MD5").replace("\"", ""));
          }
          Logger.logInfo("Downloading " + asset.name);
          Logger.logDebug(asset.name);
          Logger.logDebug("RemoteSize: " + remoteSize);
          Logger.logDebug("asset.hash: " + asset.hash);
          Logger.logDebug("remoteHash: " + remoteHash);

          // existing file are only added when we want to check file integrity with force update
          if (asset.local.exists()) {
            long localSize = asset.local.length();
            if (!(con instanceof HttpURLConnection && localSize == remoteSize)) {
              asset.local.delete();
              Logger.logInfo("Local asset size differs from remote size: " + asset.name
                  + " remote: " + remoteSize + " local: " + localSize);
            }
          }

          if (asset.local.exists()) {
            AssetDownloader.doHashCheck(asset, remoteHash);
          }

          if (asset.local.exists()) {
            downloadSuccess = true;
            totalBytesRead += remoteSize;
            continue;
          }


          // download if needed

          con = asset.url.openConnection();
          if (con instanceof HttpURLConnection) {
            con.setRequestProperty("Cache-Control", "no-cache, no-transform");
            ((HttpURLConnection) con).setRequestMethod("GET");
            con.connect();
          }
          asset.local.getParentFile().mkdirs();
          int readLen;
          long currentSize = 0;
          final double BYTES_PER_KILOBYTE = 1024;
          final double NANOS_PER_SECOND = 1000000000.0;
          InputStream input = con.getInputStream();
          FileOutputStream output = new FileOutputStream(asset.local);
          while ((readLen = input.read(buffer, 0, BUFFER_SIZE)) != -1) {
            /*
             * if (AssetDownloader.instance.isCancelled()) { input.close(); output.close();
             * asset.local.delete(); return; }
             */
            output.write(buffer, 0, readLen);
            currentSize += readLen;
            totalBytesRead += readLen;

            int prog = (int) ((currentSize * 100) / remoteSize);
            if (prog > 100) {
              prog = 100;
            }
            if (prog < 0) {
              prog = 0;
            }

            speed =
                NANOS_PER_SECOND / BYTES_PER_KILOBYTE * totalBytesRead
                    / (System.nanoTime() - start + 1);
            /*
             * if (instance.totalSize > 0) {
             * instance.setTotalProgress(instance.calculateTotalProgress(currentSize, remoteSize));
             * }
             */

          }

          input.close();
          output.close();

          // setIndeterminate();

          // file downloaded check size
          if (!(con instanceof HttpURLConnection && currentSize > 0 && currentSize == remoteSize)) {
            asset.local.delete();
            Logger.logInfo("Local asset size differs from remote size: " + asset.name + " remote: "
                + remoteSize + " local: " + currentSize);
          }

          if (downloadSuccess = AssetDownloader.doHashCheck(asset, remoteHash)) {
          }

        } catch (Exception e) {
          downloadSuccess = false;
          e.printStackTrace();
          Logger.logError("Connection failed, trying again");
        }
      }
      if (!downloadSuccess) {
        allDownloaded = false;
      }
      currentFile++;
      updateStatus();
      /*
       * if (instance.totalSize == 0) { instance.setTotalProgress(instance.calculateTotalProgress(0,
       * 0)); }
       */
    }

  }

  public static void updateStatus() {
    System.out.printf("Downloaded %d of %d speed %.2f KB/s\n", currentFile, amountOfFiles, speed);
  }

}
