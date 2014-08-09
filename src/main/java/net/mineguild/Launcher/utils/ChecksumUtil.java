package net.mineguild.Launcher.utils;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

public class ChecksumUtil {

    private static ConcurrentHashMap<File, Long> results;

    public static Map<File, Long> getChecksum(List<File> files){
        ExecutorService executor = Executors.newFixedThreadPool(4);
        results = new ConcurrentHashMap<>();
        for (File file : files) {
            try {
                Runnable worker = new WorkerTask(file);
                executor.execute(worker);
            } catch (Exception ignored){
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

    public static long getChecksum(File file) throws IOException {
        return Files.hash(file, Hashing.adler32()).asLong();
    }

    public static String getMD5(File file) throws IOException {
        return Files.hash(file, Hashing.md5()).toString();
    }

    public static String getSHA(File file) throws IOException {
        return Files.hash(file, Hashing.sha1()).toString();
    }

    public static String getMD5(String str) {
        try {
            HashFunction hf = Hashing.md5();
            HashCode hc = hf.hashString(str, Charset.forName("utf-8"));
            return hc.toString();
        } catch (Exception ignored){}
        return null;
    }

    public static class WorkerTask implements Runnable {

        File file;

        WorkerTask(File file){
            this.file = file;
        }

        @Override
        public void run(){
            try {
                results.put(file, getChecksum(file));
            } catch (Exception ignored){}

        }
    }

}
