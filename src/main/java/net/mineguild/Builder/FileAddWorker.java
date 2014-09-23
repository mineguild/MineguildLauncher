package net.mineguild.Builder;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import javax.swing.SwingWorker;

import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.utils.ChecksumUtil;
import net.mineguild.Launcher.utils.ChecksumUtil.ModPackEntry;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.Parallel;
import net.mineguild.ModPack.ModPackFile;

import com.google.common.collect.Maps;


public class FileAddWorker extends SwingWorker<Map<String, ModPackFile>, Void> {

  @Override
  protected void done() {
    super.done();
    firePropertyChange("done", null, true);
  }

  private Collection<File> files;
  private float progressPerFile;
  private File baseDirectory;
  private int progress;

  public FileAddWorker(Collection<File> files, File baseDirectory) {
    this.files = files;
    this.baseDirectory = baseDirectory;
    this.progressPerFile = 100f / files.size();
  }

  public void updateProgress() {
    setProgress((int) (progressPerFile * ++progress));
  }


  @Override
  protected Map<String, ModPackFile> doInBackground() throws Exception {
    Collection<ModPackEntry> result = new Parallel.ForEach<File, ModPackEntry>(files).withFixedThreads(OSUtils.getNumCores() * 2).apply(new Parallel.F<File, ModPackEntry>() {
      
      @Override
      public ModPackEntry apply(File e) {
        try {
          ModPackEntry entry = ChecksumUtil.getFile(baseDirectory, e);
          updateProgress();
          return entry;
        } catch (Exception e1) {
          Logger.logError("Unable to add file!", e1);
        }
        return null;
      }
    }).values();
    Map<String, ModPackFile> ret = Maps.newTreeMap();
    for(ModPackEntry entry : result){
      ret.put(entry.getKey(), entry.getValue());
    }
    return ret;
    /*
    ExecutorService executor = Executors.newFixedThreadPool(OSUtils.getNumCores());
    for (File file : files) {
      try {
        Runnable worker = new WorkerTask(file, Hashing.md5());
        executor.execute(worker);
      } catch (Exception ignored) {
      }

    }
    executor.shutdown();
    try {
      executor.awaitTermination(60, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
    return results;*/
  }
  /*
   * public static class WorkerTask implements Runnable {
   * 
   * File file; HashFunction hf;
   * 
   * WorkerTask(File file, HashFunction hf) { this.file = file; this.hf = hf; }
   * 
   * @Override public void run() { try { results.put(file, Files.hash(file, hf).toString());
   * FileAddWorker.instance.updateProgress(); } catch (Exception ignored) { }
   * 
   * } }
   */

}
