package net.mineguild;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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

    public static long getChecksum(File file) throws Exception {
        CheckedInputStream cis = new CheckedInputStream(new FileInputStream(file), new Adler32());
        byte[] buffer = new byte[128];
        int bytesRead;
        do {
            bytesRead = cis.read(buffer);
        } while (bytesRead >= 0);
        return cis.getChecksum().getValue();
    }

    public static String getMD5(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = new FileInputStream(file)) {
            DigestInputStream dis = new DigestInputStream(is, md);
            byte[] buffer = new byte[1024];
            int bytesRead;
            do {
                bytesRead = dis.read(buffer);
            } while (bytesRead >= 0);
        }
        byte[] hash = md.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte aHash : hash) {
            if ((0xff & aHash) < 0x10) {
                hexString.append("0").append(Integer.toHexString((0xFF & aHash)));
            } else {
                hexString.append(Integer.toHexString(0xFF & aHash));
            }
        }
        return hexString.toString();
    }

    public static String getMD5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] hash = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte aHash : hash) {
                if ((0xff & aHash) < 0x10) {
                    hexString.append("0").append(Integer.toHexString((0xFF & aHash)));
                } else {
                    hexString.append(Integer.toHexString(0xFF & aHash));
                }
            }
            return hexString.toString();
        } catch (Exception ignored){}
        return  null;
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
