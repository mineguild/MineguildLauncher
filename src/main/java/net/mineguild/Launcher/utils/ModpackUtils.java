package net.mineguild.Launcher.utils;

import net.mineguild.Launcher.MineguildLauncher;
import net.mineguild.Launcher.Modpack;
import net.mineguild.Launcher.download.DownloadDialog;
import net.mineguild.Launcher.download.DownloadInfo;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ModpackUtils {
  public static Map<String, String> needed;

  public static void updateModpack(Modpack currentPack, Modpack newPack) throws Exception {
    if(currentPack == null){
      FileUtils.cleanDirectory(new File(getGameDir(), "mods"));
      FileUtils.cleanDirectory(new File(getGameDir(), "config"));
    } else{
      deleteOldFiles(getGameDir(), newPack.getModpackFiles(),
          currentPack.getOld(newPack));
    }
    Map<String, String> neededFiles =
        getNeededFiles(getGameDir(), newPack.getModpackFiles(),
            MineguildLauncher.doExactCheck);
    if (neededFiles.size() > 0) {
      List<DownloadInfo> info =
          DownloadInfo.getDownloadInfo(getGameDir(), neededFiles);
      DownloadDialog dialog =
          new DownloadDialog(info, "Updating Modpack", DownloadInfo.getTotalSize(neededFiles.values()));
      dialog.setVisible(true);
      dialog.start();
      dialog.dispose();
    }
  }

  public static void updateModpack(Modpack newPack) throws Exception {
    updateModpack(null, newPack);
  }

  public static Map<String, String> getNeededFiles(File baseDirectory, Map<String, String> files,
      boolean exactCheck) {
    needed = new HashMap<>();
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    for (Map.Entry<String, String> entry : files.entrySet()) {
      try {
        Runnable worker =
            new NeededFilesTask(new File(baseDirectory, entry.getKey()), entry.getKey(),
                entry.getValue(), exactCheck);
        executorService.execute(worker);
      } catch (Exception ignored) {
      }
    }
    executorService.shutdown();
    try {
      executorService.awaitTermination(60, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
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
            System.out.println("Deleting: " + currentFile.getName());
          }
        } else {
          FileUtils.deleteQuietly(currentFile);
          System.out.println("Deleting: " + currentFile.getName());
        }
      }
    }
  }
  
  public static File getGameDir(){
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
