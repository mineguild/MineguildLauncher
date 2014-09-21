package net.mineguild.Launcher.utils;

import static net.mineguild.Launcher.utils.RelativePath.getRelativePath;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.mineguild.Launcher.log.Logger;
import net.mineguild.ModPack.ModPackFile;

import com.google.common.collect.Maps;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class ChecksumUtil {

  private static ConcurrentHashMap<File, String> results;

  public static Map<File, String> getChecksum(List<File> files, HashFunction hf) {
    ExecutorService executor = Executors.newFixedThreadPool(4);
    results = new ConcurrentHashMap<File, String>();
    for (File file : files) {
      try {
        Runnable worker = new WorkerTask(file, hf);
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


  public static synchronized Map<String, ModPackFile> getFiles(final File baseDirectory,
      Collection<File> files) throws InterruptedException, ExecutionException {
    Collection<Entry<String, ModPackFile>> results = new Parallel.ForEach<File, Entry<String, ModPackFile>>(files)
            .withFixedThreads(2 * OSUtils.getNumCores()).apply(new Parallel.F<File, Entry<String, ModPackFile>>() {
              @Override
              public Entry<String, ModPackFile> apply(final File e) {
                try {
                  final String hash = getMD5(e);
                  final String path = getRelativePath(baseDirectory, e);
                  
                  return new Entry<String, ModPackFile>() {
                    
                    ModPackFile value = new ModPackFile(hash, e.length());
                    
                    @Override
                    public ModPackFile setValue(ModPackFile value) {
                      ModPackFile old = this.value;
                      this.value = value;
                      return old;
                    }
                    
                    @Override
                    public ModPackFile getValue() {
                      return value;
                    }
                    
                    @Override
                    public String getKey() {
                      return path;
                    }
                  };
                } catch (Exception e1) {
                  Logger.logError("Exception while trying to process file!", e1);
                  return null;
                }

              }
            }).values();
    Map<String, ModPackFile> ret = Maps.newTreeMap();
    for(Entry<String, ModPackFile> e : results){
      ret.put(e.getKey(), e.getValue());
    }
    return ret;
  }

  public static String getHash(HashFunction hf, String str) {
    return hf.hashString(str, Charset.forName("utf-8")).toString();
  }

  public static String getChecksum(File file) throws IOException {
    return Files.hash(file, Hashing.adler32()).toString();
  }

  public static String getMD5(File file) throws IOException {
    return Files.hash(file, Hashing.md5()).toString();
  }

  public static String getSHA(File file) throws IOException {
    return Files.hash(file, Hashing.sha1()).toString();
  }

  public static String getMD5(String str) {
    HashFunction hf = Hashing.md5();
    return hf.hashString(str, Charset.forName("utf-8")).toString();
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
      } catch (Exception ignored) {
      }

    }
  }

}
