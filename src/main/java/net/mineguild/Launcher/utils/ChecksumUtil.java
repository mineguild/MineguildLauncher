package net.mineguild.Launcher.utils;

import static net.mineguild.Launcher.utils.RelativePath.getRelativePath;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.mineguild.Launcher.log.Logger;
import net.mineguild.ModPack.ModPack;
import net.mineguild.ModPack.ModPackFile;

import org.apache.commons.lang3.NotImplementedException;

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


    /**
     * Creates a {@link Map} [ {@link String} : {@link ModPackFile} ] from the list of files and the
     * relative directory.
     *
     * @param baseDirectory The directory the pack is located (containing config and mods directory).
     *                      This is used for the relative path.
     * @param files         a {@link Collection} of {@link File} to use
     * @return the resulting {@link Map}
     * @throws InterruptedException if thread was interrupted
     * @throws ExecutionException   if some other kind of {@link Exception} occurred while trying to
     *                              execute this.
     * @see ModPack
     */
    public static synchronized Map<String, ModPackFile> getFiles(final File baseDirectory,
        Collection<File> files) throws InterruptedException, ExecutionException {
        Collection<Map.Entry<String, ModPackFile>> results =
            new Parallel.ForEach<File, Map.Entry<String, ModPackFile>>(files)
                .withFixedThreads(2 * OSUtils.getNumCores())
                .apply(new Parallel.F<File, Map.Entry<String, ModPackFile>>() {
                    @Override public Map.Entry<String, ModPackFile> apply(final File e) {
                        try {
                            return getFile(baseDirectory, e);
                        } catch (Exception e1) {
                            Logger.logError("Unable to add file to pack!", e1);
                        }
                        return null;
                    }
                }).values();
        Map<String, ModPackFile> ret = Maps.newTreeMap();
        for (Map.Entry<String, ModPackFile> e : results) {
            ret.put(e.getKey(), e.getValue());
        }
        return ret;
    }

    /**
     * Creates {@link ModPackEntry} from given file and baseDirectory.
     *
     * @param baseDirectory The directory the pack is located (containing config and mods directory).
     *                      This is used for the relative path.
     * @param f             The file to get the path, size and hash from
     * @return resulting {@link ModPackEntry}
     * @throws Exception If an {@link Exception} of any kind occurs
     */
    public static ModPackEntry getFile(File baseDirectory, File f) throws Exception {
        return new ModPackEntry(getRelativePath(baseDirectory, f),
            new ModPackFile(getMD5(f), f.length()));
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

        @Override public void run() {
            try {
                results.put(file, Files.hash(file, hf).toString());
            } catch (Exception ignored) {
            }

        }
    }


    public static class ModPackEntry implements Map.Entry<String, ModPackFile> {

        final String key;
        final ModPackFile value;

        public ModPackEntry(String key, ModPackFile value) {
            this.key = key;
            this.value = value;
        }

        @Override public String getKey() {
            return key;
        }

        @Override public ModPackFile getValue() {
            return value;
        }

        @Override public ModPackFile setValue(ModPackFile value) {
            throw new NotImplementedException("Not in this implementation!");
        }

    }


    public static class Entry<T> implements Map.Entry<String, T> {

        final String key;
        final T value;

        public Entry(String key, T value) {
            this.key = key;
            this.value = value;
        }

        @Override public String getKey() {
            return key;
        }

        @Override public T getValue() {
            return value;
        }

        @Override public T setValue(T value) {
            throw new NotImplementedException("Not in this implementation!");
        }

    }

}
