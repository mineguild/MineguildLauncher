package net.mineguild.Launcher.utils;

import net.mineguild.Launcher.MineguildLauncher;
import net.mineguild.Launcher.Modpack;
import net.mineguild.Launcher.download.DownloadDialog;
import net.mineguild.Launcher.download.DownloadInfo;
import net.mineguild.Launcher.download.MultithreadDownloadDialog;
import net.mineguild.Launcher.log.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ModpackUtils {
  public static Map<String, String> needed;

  public static void updateModpack(Modpack currentPack, Modpack newPack) throws Exception {
    if (currentPack == null) {
      deleteUntrackedMods(getGameDir(), newPack.getModpackFiles());
    } else {
      deleteOldFiles(getGameDir(), newPack.getModpackFiles(), currentPack.getOld(newPack));
    }
    Map<String, String> neededFiles =
        getNeededFiles(getGameDir(), newPack.getModpackFiles(), MineguildLauncher.doExactCheck);
    if (neededFiles.size() > 0) {
      List<DownloadInfo> info = DownloadInfo.getDownloadInfo(getGameDir(), neededFiles);
      MultithreadDownloadDialog dialog =
          new MultithreadDownloadDialog(info, "Updating Modpack", DownloadUtils.getTotalSize(neededFiles
              .values()));
      long startTime = System.currentTimeMillis();
      dialog.setVisible(true);
      boolean success = dialog.start();
      if (!success) {
        throw new Exception("Modpack updater cancelled!");
      }
      MineguildLauncher.totalDownloadTime += System.currentTimeMillis()-startTime;
      dialog.dispose();
    }
  }

  public static void updateModpack(Modpack newPack) throws Exception {
    updateModpack(null, newPack);
  }

  public static Map<String, String> getNeededFiles(File baseDirectory, Map<String, String> files,
      boolean exactCheck) {
    needed = new HashMap<String, String>();
    float start = System.currentTimeMillis();
    Logger.logInfo("Checking local mods.");
    ExecutorService executorService = Executors.newFixedThreadPool(OSUtils.getNumCores()*2);
    for (Map.Entry<String, String> entry : files.entrySet()) {
      try {
        Runnable worker =
            new NeededFilesTask(new File(baseDirectory, entry.getKey()), entry.getKey(),
                entry.getValue(), exactCheck);
        executorService.execute(worker);
      } catch (Exception ignored) {
        Logger.logError("Unable to check modpack file!", ignored);
      }
    }
    executorService.shutdown();
    try {
      executorService.awaitTermination(60, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
    Logger.logInfo(String.format("Checking completed in %.2f seconds", (System.currentTimeMillis() - start)/1000));
    return needed;
  }

  public static void deleteOldFiles(File baseDirectory, Map<String, String> allFiles,
      Map<String, String> oldFiles) throws IOException {
    for (Map.Entry<String, String> entry : oldFiles.entrySet()) {
      File currentFile = new File(baseDirectory, entry.getKey());
      if (currentFile.exists()) {
        if (allFiles.containsKey(entry.getKey())) {
          String hash = ChecksumUtil.getMD5(new File(baseDirectory, entry.getKey()));
          if (!hash.equals(allFiles.get(entry.getKey()))) {
            FileUtils.deleteQuietly(currentFile);
            Logger
                .logInfo(String.format("Deleted '%s' - hash doesn't match", currentFile.getName()));
          }
        } else {
          FileUtils.deleteQuietly(currentFile);
          Logger.logInfo(String.format("Deleted '%s' - no longer in pack", currentFile.getName()));
        }
      }
    }
  }

  public static void deleteUntrackedMods(File baseDirectory, Map<String, String> files) {
    File mods = new File(baseDirectory, "mods");
    if (!mods.exists()) {
      return;
    }
    Set<String> needed = files.keySet();
    for (File f : FileUtils.listFiles(mods, FileFilterUtils.trueFileFilter(),
        FileFilterUtils.trueFileFilter())) {
      String path = FilenameUtils.separatorsToUnix(RelativePath.getRelativePath(baseDirectory, f));
      if (!needed.contains(path)) {
        FileUtils.deleteQuietly(f);
        Logger.logInfo(String.format("Deleted '%s' - not in pack", f.getName()));
      }
    }
  }

  public static File getGameDir() throws IOException {
    File gameDir = new File(MineguildLauncher.baseDirectory, "minecraft");
    if (!gameDir.exists()) {
      if (!gameDir.mkdirs()) {
        throw new IOException("Unable to create game directory!");
      }
    }
    return new File(MineguildLauncher.baseDirectory, "minecraft");
  }

  public static class NeededFilesTask implements Runnable {

    private File file;
    private String hash;
    private String name;
    private boolean exactCheck;

    public NeededFilesTask(File file, String name, String hash, boolean exactCheck) {
      this.file = file;
      this.hash = hash;
      this.name = name;
      this.exactCheck = exactCheck;
    }

    @Override
    public void run() {
      if (file.exists()) {
        try {
          if (exactCheck) {
            if (!ChecksumUtil.getMD5(file).equals(hash)) {
              needed.put(name, hash);
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        needed.put(name, hash);
      }
    }
  }
}
