package net.mineguild.Launcher.download;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker;

import lombok.Getter;
import lombok.Setter;
import net.mineguild.Launcher.Constants;
import net.mineguild.Launcher.MineguildLauncher;
import net.mineguild.Launcher.download.DownloadInfo.DLType;
import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.utils.DownloadUtils;

import org.apache.commons.io.FileUtils;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class AssetDownloader extends SwingWorker<Boolean, Void> {
    private static final int BUFFER_SIZE = 8192;
    private static AssetDownloader instance;
    private List<DownloadInfo> downloads;
    private boolean allDownloaded = true;
    private int totalProgress = 0;
    private @Setter @Getter long totalSize = 0;
    private int currentFile = 0;
    private float percentPerFile = 0;
    private long speed;
    private String speedLabel = "";
    private long totalBytesRead = 0;
    private double start;

    final static int BYTESPERMB = 1000000;
    final static int SPEED_UPDATE_RATE = 1;
    final static double NANOS_PER_SECOND = 1000000000.0;

    @Getter @Setter private boolean multithread = false;
    @Getter private String status;
    @Getter private int ready = 0;

    public AssetDownloader(List<DownloadInfo> downloads) {
        this.downloads = downloads;
        this.percentPerFile = 100 / (float) downloads.size();
        AssetDownloader.instance = this;
    }

    public AssetDownloader(List<DownloadInfo> downloads, long totalSize) {
        this(downloads);
        this.totalSize = totalSize;
    }


    public static Thread startUpdateThread(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run(){
                while(Thread.currentThread().isAlive()){
                    instance.setSpeed((long) (NANOS_PER_SECOND / 1 * instance.totalBytesRead / (
                            System.nanoTime() - instance.start + 1)));
                    try{
                        Thread.sleep(SPEED_UPDATE_RATE * 1000);
                    } catch (InterruptedException e){
                        Logger.logDebug("Sleep interrupted - no problem here(DL-TICK)", e);
                    }
                }
            }
        });
        t.start();
        return t;
    }

    @Override protected Boolean doInBackground() throws Exception {
        start = System.nanoTime();
        Thread updateThread = startUpdateThread();
        if (multithread) {
            ExecutorService executor =
                Executors.newFixedThreadPool(MineguildLauncher.getSettings().getDownloadThreads());
            if (totalSize == 0) {
                setTotalIndeterminate();
                DownloadUtils.setTotalSize(downloads, this);
            }
            for (DownloadInfo download : downloads) {
                try {
                    Runnable worker = new DownloadWorker(download);
                    executor.execute(worker);
                } catch (Exception ignored) {
                }

            }
            executor.shutdown();
            try {

                boolean done = false;
                while (!isCancelled() && !done) {
                    done = executor.awaitTermination(100, TimeUnit.MILLISECONDS);
                }
                if (isCancelled()) {
                    executor.shutdownNow();
                    allDownloaded = false;
                    Thread.currentThread().interrupt();
                }

            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        } else {
            for (DownloadInfo download : downloads) {
                if (isCancelled()) {
                    return false;
                }
                doDownload(download);
                setTotalProgress(calculateTotalProgress(0, 0));
                currentFile++;
            }
        }
        updateThread.interrupt();
        setStatus(allDownloaded ? "Success" : "Downloads failed");
        return allDownloaded;
    }

    public synchronized void setStatus(String newStatus) {
        String oldStatus = status;
        status = newStatus;
        firePropertyChange("note", oldStatus, status);
    }

    public void updateIndividualProgress(String threadId, int progress) {
        Object[] data = {threadId, progress};
        firePropertyChange("indProgress", null, data);
    }

    public void addIndividualProgress(String threadId, String fName) {
        Object[] data = {threadId, fName};
        firePropertyChange("addIndProgress", null, data);
    }

    public void removeIndividualProgress(String threadId) {
        firePropertyChange("removeIndProgress", null, threadId);
    }


    public synchronized void updateStatus(String filename) {
        HashMap<String, Object> data = Maps.newHashMap();
        data.put("fileName", filename);
        data.put("currentFile", currentFile + 1);
        data.put("overallFiles", downloads.size());
        firePropertyChange("info", null, data);
    }

    public synchronized void updateStatus() {
        HashMap<String, Object> data = Maps.newHashMap();
        data.put("currentFile", currentFile + 1);
        data.put("overallFiles", downloads.size());
        firePropertyChange("info", null, data);
    }

    public synchronized void setSpeed(long newSpeed) {
        speed = newSpeed;
        String oldSpeedLabel = speedLabel;
        speedLabel = FileUtils.byteCountToDisplaySize(speed) + "/s";
        firePropertyChange("speed", oldSpeedLabel, speedLabel);
    }

    public synchronized int calculateTotalProgress(long currentSize, long remoteSize) {
        int newProg = 0;
        if (totalSize > 0) {
            newProg = (int) ((totalBytesRead * 100) / totalSize);
        } else if (currentSize > 0 && remoteSize > 0) {
            newProg = (int) (((currentSize * (percentPerFile)) / remoteSize)
                    + percentPerFile * currentFile);
        } else {
            newProg = (int) (percentPerFile * (currentFile + 1));
        }
        if (newProg > 100) {
            return 100;
        } else if (newProg < 0) {
            return 0;
        } else {
            return newProg;
        }
    }

    public synchronized void setIndeterminate() {
        firePropertyChange("current_inter", null, true);
    }

    public synchronized void setTotalProgress(int newProgress) {
        int oldProgress = totalProgress;
        totalProgress = newProgress;
        firePropertyChange("overall", oldProgress, totalProgress);
    }

    public synchronized void setTotalIndeterminate() {
        firePropertyChange("overallIndeterminate", null, true);
    }

    private void doDownload(DownloadInfo asset) {
        byte[] buffer = new byte[BUFFER_SIZE];
        boolean downloadSuccess = false;
        List<String> remoteHash = asset.hash;
        int attempt = 0;
        final int attempts = 5;
        updateStatus(asset.name);
        setIndeterminate();
        while (!downloadSuccess && (attempt < attempts)) {
            try {
                if (remoteHash == null) {
                    remoteHash = Lists.newArrayList();
                }
                if (isCancelled()) {
                    return;
                }
                if (attempt++ > 0) {
                    Logger.logInfo(
                        "Connecting.. Try " + attempt + " of " + attempts + " for: " + asset.url);
                }

                URLConnection con = asset.url.openConnection();
                if (con instanceof HttpURLConnection) {
                    con.setRequestProperty("Cache-Control", "no-cache, no-transform");
                    if (asset.url.toString().contains(Constants.MG_GET_SCRIPT)) {
                        ((HttpURLConnection) con).setRequestMethod("GET");
                    } else {
                        ((HttpURLConnection) con).setRequestMethod("HEAD");
                    }
                    con.connect();
                }

                // gather data for basic checks
                long remoteSize = Long.parseLong(con.getHeaderField("Content-Length"));
                if (remoteSize == 0) {
                    downloadSuccess = true;
                    continue;
                }

                if (asset.hash == null && asset.getPrimaryDLType() == DLType.ETag) {
                    String eTag = con.getHeaderField("ETag").replace("\"", "");
                    remoteHash.clear();
                    remoteHash.add(eTag);

                }

                if (asset.hash == null && asset.getPrimaryDLType() == DLType.ContentMD5) {
                    remoteHash.clear();
                    remoteHash.add(con.getHeaderField("Content-MD5").replace("\"", ""));
                }
                Logger.logInfo("Downloading " + asset.name);
                Logger.logDebug(asset.name);
                Logger.logDebug("RemoteSize: " + remoteSize);
                Logger.logDebug("asset.hash: " + asset.hash);
                Logger.logDebug("remoteHash: " + remoteHash);

                // existing file are only added when we want to check file integrity with force update
                if (asset.local.exists()) {
                    long localSize = asset.local.length();
                    if (!(con instanceof HttpURLConnection && localSize == remoteSize)) {
                        asset.local.delete();
                        Logger.logInfo(
                            "Local asset size differs from remote size: " + asset.name + " remote: "
                                + remoteSize + " local: " + localSize);
                    }
                }

                if (asset.local.exists()) {
                    doHashCheck(asset, remoteHash);
                }

                if (asset.local.exists()) {
                    downloadSuccess = true;
                    totalBytesRead += remoteSize;
                    continue;
                }

                // download if needed

                con = asset.url.openConnection();
                if (con instanceof HttpURLConnection) {
                    con.setRequestProperty("Cache-Control", "no-cache, no-transform");
                    ((HttpURLConnection) con).setRequestMethod("GET");
                    con.connect();
                }
                asset.local.getParentFile().mkdirs();
                int readLen;
                long currentSize = 0;
                if (totalSize == 0) {
                    start = System.nanoTime();
                }

                InputStream input = con.getInputStream();
                FileOutputStream output = new FileOutputStream(asset.local);
                while ((readLen = input.read(buffer, 0, BUFFER_SIZE)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        output.close();
                        asset.local.delete();
                        return;
                    }
                    output.write(buffer, 0, readLen);
                    currentSize += readLen;
                    totalBytesRead += readLen;

                    int prog = (int) ((currentSize * 100) / remoteSize);
                    if (prog > 100) {
                        prog = 100;
                    }
                    if (prog < 0) {
                        prog = 0;
                    }
                    /*if (totalSize == 0) {
                        setSpeed(
                            (long) (NANOS_PER_SECOND / 1 * currentSize / (System.nanoTime() - start
                                + 1)));
                    } else {
                        setSpeed((long) (NANOS_PER_SECOND / 1 * totalBytesRead / (
                            System.nanoTime() - start + 1)));
                    }*/

                    setProgress(prog);
                    setTotalProgress(calculateTotalProgress(currentSize, remoteSize));

                }

                input.close();
                output.close();

                setIndeterminate();

                // file downloaded check size
                if (!(con instanceof HttpURLConnection && currentSize > 0
                    && currentSize == remoteSize)) {
                    asset.local.delete();
                    Logger.logInfo(
                        "Local asset size differs from remote size: " + asset.name + " remote: "
                            + remoteSize + " local: " + currentSize);
                }

                if (downloadSuccess = doHashCheck(asset, remoteHash)) {
                }
            } catch (Exception e) {
                downloadSuccess = false;
                asset.local.delete();
                e.printStackTrace();
                Logger.logError("Connection failed, trying again");
            }
        }
        if (!downloadSuccess) {
            allDownloaded = false;
        }
    }

    public static boolean doHashCheck(DownloadInfo asset, final List<String> remoteHash)
        throws IOException {
        String hash = DownloadUtils.fileHash(asset.local, asset.hashType).toLowerCase();
        List<String> assetHash = asset.hash;
        boolean good = false;
        if (asset.hash == null) {
            if (remoteHash != null) {
                assetHash = remoteHash;
            }
        }
        if (good || assetHash != null && assetHash.contains(hash)) {
            return true;
        }
        Logger.logInfo("Asset hash checking failed: " + asset.name + " " + asset.hashType + " "
            + hash);// unhashed
        // DL's
        // are
        // not
        // allowed!!!
        asset.local.delete();
        return false;
    }

    public static class DownloadWorker implements Runnable {

        DownloadInfo asset;
        public int prog;

        public DownloadWorker(DownloadInfo asset) {
            this.asset = asset;
        }

        @Override public void run() {
            byte[] buffer = new byte[BUFFER_SIZE];
            boolean downloadSuccess = false;
            List<String> remoteHash = asset.hash;
            int attempt = 0;
            final String threadId = Thread.currentThread().getName();

            final int attempts = 5;
            while (!downloadSuccess && (attempt < attempts)) {
                try {
                    if (remoteHash == null) {
                        remoteHash = Lists.newArrayList();
                    }
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    if (attempt++ > 0) {
                        Logger.logInfo("Connecting.. Try " + attempt + " of " + attempts + " for: "
                            + asset.url);
                    }

                    URLConnection con = asset.url.openConnection();
                    if (con instanceof HttpURLConnection) {
                        con.setRequestProperty("Cache-Control", "no-cache, no-transform");
                        if (asset.url.toString().contains(Constants.MG_GET_SCRIPT)) {
                            ((HttpURLConnection) con).setRequestMethod("GET");
                        } else {
                            ((HttpURLConnection) con).setRequestMethod("HEAD");
                        }
                        con.connect();
                    }

                    // gather data for basic checks
                    long remoteSize = Long.parseLong(con.getHeaderField("Content-Length"));
                    if (remoteSize == 0) {
                        downloadSuccess = true;
                        continue;
                    }

                    if (asset.hash == null && asset.getPrimaryDLType() == DLType.ETag) {
                        String eTag = con.getHeaderField("ETag").replace("\"", "");
                        remoteHash.clear();
                        remoteHash.add(eTag);

                    }

                    if (asset.hash == null && asset.getPrimaryDLType() == DLType.ContentMD5) {
                        remoteHash.clear();
                        remoteHash.add(con.getHeaderField("Content-MD5").replace("\"", ""));
                    }
                    Logger.logInfo("Downloading " + asset.name);
                    Logger.logDebug(asset.name);
                    Logger.logDebug("RemoteSize: " + remoteSize);
                    Logger.logDebug("asset.hash: " + asset.hash);
                    Logger.logDebug("remoteHash: " + remoteHash);

                    // existing file are only added when we want to check file integrity with force update
                    if (asset.local.exists()) {
                        long localSize = asset.local.length();
                        if (!(con instanceof HttpURLConnection && localSize == remoteSize)) {
                            asset.local.delete();
                            Logger.logInfo(
                                "Local asset size differs from remote size: " + asset.name
                                    + " remote: " + remoteSize + " local: " + localSize);
                        }
                    }

                    if (asset.local.exists()) {
                        AssetDownloader.doHashCheck(asset, remoteHash);
                    }

                    if (asset.local.exists()) {
                        downloadSuccess = true;
                        instance.totalBytesRead += remoteSize;
                        continue;
                    }

                    // download if needed

                    con = asset.url.openConnection();
                    if (con instanceof HttpURLConnection) {
                        con.setRequestProperty("Cache-Control", "no-cache, no-transform");
                        ((HttpURLConnection) con).setRequestMethod("GET");
                        con.connect();
                    }
                    asset.local.getParentFile().mkdirs();
                    int readLen;
                    long currentSize = 0;
                    boolean showIndProgress =
                        (remoteSize / BYTESPERMB) >= 1; // Make sure only files worth
                    // showing are shown.
                    InputStream input = con.getInputStream();
                    FileOutputStream output = new FileOutputStream(asset.local);
                    if (showIndProgress) {
                        instance.addIndividualProgress(threadId, asset.name);
                    }
                    while ((readLen = input.read(buffer, 0, BUFFER_SIZE)) != -1) {
                        if (Thread.currentThread().isInterrupted()) {
                            input.close();
                            output.close();
                            asset.local.delete();
                            if (showIndProgress) {
                                instance.removeIndividualProgress(threadId);
                            }
                            return;
                        }
                        output.write(buffer, 0, readLen);
                        currentSize += readLen;
                        AssetDownloader.instance.totalBytesRead += readLen;

                        prog = (int) ((currentSize * 100) / remoteSize);
                        if (prog > 100) {
                            prog = 100;
                        }
                        if (prog < 0) {
                            prog = 0;
                        }
                        if (showIndProgress) {
                            instance.updateIndividualProgress(threadId, prog);
                        }
                        /*
                        instance.setSpeed((long) (NANOS_PER_SECOND / 1 * instance.totalBytesRead / (
                            System.nanoTime() - instance.start + 1)));*/

                        if (instance.totalSize > 0) {
                            instance.setTotalProgress(
                                instance.calculateTotalProgress(currentSize, remoteSize));
                        }

                    }
                    if (showIndProgress) {
                        instance.removeIndividualProgress(threadId);
                    }
                    input.close();
                    output.close();

                    // file downloaded check size
                    if (!(con instanceof HttpURLConnection && currentSize > 0
                        && currentSize == remoteSize)) {
                        asset.local.delete();
                        Logger.logInfo(
                            "Local asset size differs from remote size: " + asset.name + " remote: "
                                + remoteSize + " local: " + currentSize);
                    }

                    if (downloadSuccess = AssetDownloader.doHashCheck(asset, remoteHash)) {
                    }
                } catch (Exception e) {
                    downloadSuccess = false;
                    asset.local.delete();
                    Logger.logError("Download failed, trying again", e);
                }
            }
            if (!downloadSuccess) {
                instance.allDownloaded = false;
            }
            instance.currentFile++;
            instance.updateStatus();
      /*
       * if (instance.totalSize == 0) { instance.setTotalProgress(instance.calculateTotalProgress(0,
       * 0)); }
       */
        }

    }


}
