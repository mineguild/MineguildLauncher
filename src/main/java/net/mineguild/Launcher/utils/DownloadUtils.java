package net.mineguild.Launcher.utils;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DownloadUtils {

    public static Map<String, String> needed;

    public static String fileHash (File file, String type) throws IOException {
        if (!file.exists()) {
            return "";
        }
        if(type.equalsIgnoreCase("md5"))
            return ChecksumUtil.getMD5(file);
        if(type.equalsIgnoreCase("sha1"))
            return ChecksumUtil.getSHA(file);
        URL fileUrl = file.toURI().toURL();
        MessageDigest dgest = null;
        try {
            dgest = MessageDigest.getInstance(type);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        InputStream str = fileUrl.openStream();
        byte[] buffer = new byte[65536];
        int readLen;
        while ((readLen = str.read(buffer, 0, buffer.length)) != -1) {
            dgest.update(buffer, 0, readLen);
        }
        str.close();
        Formatter fmt = new Formatter();
        for (byte b : dgest.digest()) {
            fmt.format("%02X", b);
        }
        String result = fmt.toString();
        fmt.close();
        return result;
    }

    public static class NeededFilesTask implements Runnable {

        private File file;
        private String hash;
        private String name;
        private boolean exactCheck;

        public NeededFilesTask(File file, String name, String hash, boolean exactCheck){
            this.file = file;
            this.hash = hash;
            this.name = name;
            this.exactCheck = exactCheck;
        }

        @Override
        public void run() {
            if(file.exists()){
                try {
                    if(exactCheck) {
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

    public static Map<String, String> getNeededFiles(File baseDirectory, Map<String, String> files, boolean exactCheck){
        needed = new HashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        for(Map.Entry<String, String> entry : files.entrySet()){
            try{

                Runnable worker = new NeededFilesTask(new File(baseDirectory, entry.getKey()), entry.getKey(), entry.getValue(), exactCheck);
                executorService.execute(worker);
            } catch (Exception ignored){}
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

    public static void deleteUnneededFiles(File baseDirectory, Map<String, String> allFiles, Map<String, String> oldFiles) throws IOException {
        for(Map.Entry<String, String> entry : oldFiles.entrySet()){
            File currentFile = new File(baseDirectory, entry.getKey());
            if(currentFile.exists()) {
                if (allFiles.containsKey(entry.getKey())) {
                    String hash = ChecksumUtil.getMD5(new File(baseDirectory, entry.getKey()));
                    if (!hash.equals(allFiles.get(entry.getKey()))) {
                        FileUtils.deleteQuietly(currentFile);
                    }
                } else {
                    FileUtils.deleteQuietly(currentFile);
                }
            }
        }
    }






}
