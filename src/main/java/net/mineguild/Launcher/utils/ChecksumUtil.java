package net.mineguild.Launcher.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.mineguild.Launcher.log.Logger;
import net.mineguild.ModPack.ModPackFile;

import com.google.common.collect.Sets;
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


  public static synchronized Set<ModPackFile> getFiles(final File baseDirectory,
      Collection<File> files) throws InterruptedException, ExecutionException {
    Set<ModPackFile> results =
        Sets.newTreeSet(new Parallel.ForEach<File, ModPackFile>(files)
            .withFixedThreads(2 * OSUtils.getNumCores()).apply(new Parallel.F<File, ModPackFile>() {
              @Override
              public ModPackFile apply(File e) {
                try {
                  String hash = getMD5(e);
                  return new ModPackFile(baseDirectory, e, hash);
                } catch (Exception e1) {
                  Logger.logError("Exception while trying to process file!", e1);
                  return null;
                }

              }
            }).values());
    return results;
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
