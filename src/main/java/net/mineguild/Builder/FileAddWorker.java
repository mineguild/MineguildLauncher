package net.mineguild.Builder;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;


public class FileAddWorker extends SwingWorker<Map<File, String>, Void> {

  @Override
  protected void done() {
    super.done();
    firePropertyChange("done", null, true);
  }

  private static ConcurrentHashMap<File, String> results;
  private static FileAddWorker instance;
  private Collection<File> files;
  private float progressPerFile;

  public FileAddWorker(Collection<File> files) {
    this.files = files;
    this.progressPerFile = 100f / files.size();
    FileAddWorker.instance = this;
  }

  public void updateProgress() {
    setProgress((int) (progressPerFile * results.size()));
  }


  @Override
  protected Map<File, String> doInBackground() throws Exception {
    results = new ConcurrentHashMap<File, String>();
    ExecutorService executor = Executors.newFixedThreadPool(4);
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
    return results;
  }

  public static class WorkerTask implements Runnable {

    File file;
    HashFunction hf;

    WorkerTask(File file, HashFunction hf) {
      this.file = file;
      this.hf = hf;
    }

    @Override
    public void run() {
      try {
        results.put(file, Files.hash(file, hf).toString());
        FileAddWorker.instance.updateProgress();
      } catch (Exception ignored) {
      }

    }
  }

}
