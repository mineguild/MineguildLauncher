package net.mineguild.Launcher.download;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingWorker;

import lombok.Getter;
import net.mineguild.Launcher.download.DownloadInfo.DLType;
import net.mineguild.Launcher.utils.DownloadUtils;

import com.google.common.collect.Lists;

public class AssetDownloader extends SwingWorker<Boolean, Void> {
  private static final int BUFFER_SIZE = 1024;
  private static final int SPEED_UPDATE_INTERVAL = 500;
  private List<DownloadInfo> downloads;
  private boolean allDownloaded = true;
  private int totalProgress = 0;
  private long totalSize = 0;
  private int currentFile = 0;
  private float percentPerFile = 0;
  private float speed;
  private long totalBytesRead = 0;

  @Getter
  private String status;
  @Getter
  private int ready = 0;

  public AssetDownloader(List<DownloadInfo> downloads) {
    this.downloads = downloads;
    this.percentPerFile = 100 / (float) downloads.size();
  }

  public AssetDownloader(List<DownloadInfo> downloads, long totalSize) {
    this(downloads);
    this.totalSize = totalSize;
  }

  @Override
  protected Boolean doInBackground() throws Exception {
    for (DownloadInfo download : downloads) {
      if (isCancelled()) {
        return false;
      }
      doDownload(download);
      setTotalProgress(calculateTotalProgress(0, 0));
      currentFile++;
    }
    setStatus(allDownloaded ? "Success" : "Downloads failed");
    return allDownloaded;
  }

  public synchronized void setStatus(String newStatus) {
    String oldStatus = status;
    status = newStatus;
    firePropertyChange("note", oldStatus, status);
  }

  public synchronized void updateStatus(String filename) {
    HashMap<String, Object> data = new HashMap<>();
    data.put("fileName", filename);
    data.put("currentFile", currentFile + 1);
    data.put("overallFiles", downloads.size());
    firePropertyChange("info", null, data);
  }

  public synchronized void setSpeed(float newSpeed) {
    float oldSpeed = speed;
    speed = newSpeed;
    firePropertyChange("speed", oldSpeed, speed);
  }

  public int calculateTotalProgress(long currentSize, long remoteSize) {
    if (totalSize > 0) {
      return (int) ((totalBytesRead * 100) / totalSize);
    } else if (currentSize > 0 && remoteSize > 0) {
      return (int) (((currentSize * (percentPerFile)) / remoteSize) + percentPerFile * currentFile);
    } else {
      return (int) (percentPerFile * (currentFile + 1));
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

  private void doDownload(DownloadInfo asset) {
    byte[] buffer = new byte[BUFFER_SIZE];
    boolean downloadSuccess = false;
    List<String> remoteHash = asset.hash;
    String hashType;
    int attempt = 0;
    final int attempts = 5;
    updateStatus(asset.name);
    setIndeterminate();
    while (!downloadSuccess && (attempt < attempts)) {
      try {
        if (remoteHash == null) {
          remoteHash = Lists.newArrayList();
        }
        if(isCancelled()){
          return;
        }
        hashType = asset.hashType;
        if (attempt++ > 0) {
          System.out.println("Connecting.. Try " + attempt + " of " + attempts + " for: "
              + asset.url);
        }

        // Will this break something?
        // HTTPURLConnection con = (HttpURLConnection) asset.url.openConnection();
        URLConnection con = asset.url.openConnection();
        if (con instanceof HttpURLConnection) {
          con.setRequestProperty("Cache-Control", "no-cache, no-transform");
          if (asset.url.toString().contains(DownloadInfo.GET_SCRIPT)) {
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
          if (!eTag.contains("-") && eTag.length() == 32) {
            remoteHash.clear();
            remoteHash.add(eTag);
            hashType = "md5";
          }
        }
        if (asset.hash == null && asset.getPrimaryDLType() == DLType.ContentMD5) {
          remoteHash.clear();
          remoteHash.add(con.getHeaderField("Content-MD5").replace("\"", ""));
          hashType = "md5";
        }

        System.out.println(asset.name);
        System.out.println("RemoteSize: " + remoteSize);
        System.out.println("asset.hash: " + asset.hash);
        System.out.println("remoteHash: " + remoteHash);
        System.out.println("------------");

        // existing file are only added when we want to check file integrity with force update
        if (asset.local.exists()) {
          long localSize = asset.local.length();
          if (!(con instanceof HttpURLConnection && localSize == remoteSize)) {
            asset.local.delete();
            System.out.println("Local asset size differs from remote size: " + asset.name
                + " remote: " + remoteSize + " local: " + localSize);
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
        long lastTime = System.currentTimeMillis();
        float lastSize = 0;
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

          int prog = (int) ((currentSize * 100) / remoteSize);
          if (prog > 100) {
            prog = 100;
          }
          if (prog < 0) {
            prog = 0;
          }

          if (System.currentTimeMillis() - lastTime > SPEED_UPDATE_INTERVAL) {
            setSpeed(((currentSize - lastSize) / BUFFER_SIZE)
                / (System.currentTimeMillis() - lastTime) * 1024);
            lastTime = System.currentTimeMillis();
            lastSize = currentSize;
          }
          setProgress(prog);
          setTotalProgress(calculateTotalProgress(currentSize, remoteSize));
          totalBytesRead += readLen;

        }

        input.close();
        output.close();

        setIndeterminate();

        // file downloaded check size
        if (!(con instanceof HttpURLConnection && currentSize > 0 && currentSize == remoteSize)) {
          asset.local.delete();
          System.out.println("Local asset size differs from remote size: " + asset.name
              + " remote: " + remoteSize + " local: " + currentSize);
        }

        if (downloadSuccess = doHashCheck(asset, remoteHash)) {
        }
      } catch (Exception e) {
        downloadSuccess = false;
        e.printStackTrace();
        System.out.println("Connection failed, trying again");
      }
    }
    if (!downloadSuccess) {
      allDownloaded = false;
    }
  }

  public boolean doHashCheck(DownloadInfo asset, final List<String> remoteHash) throws IOException {
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
    System.out.println("Asset hash checking failed: " + asset.name + " " + asset.hashType + " "
        + hash);// unhashed DL's are not allowed!!!
    asset.local.delete();
    return false;
  }
}
