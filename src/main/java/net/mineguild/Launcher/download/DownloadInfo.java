package net.mineguild.Launcher.download;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

import net.mineguild.Launcher.Constants;

import com.google.common.collect.Lists;

public class DownloadInfo {
  public URL url;
  public File local;
  public String name;
  public long size = 0;
  public List<String> hash;
  public String hashType;
  private DLType primaryDLType = DLType.ETag;
  private DLType backupDLType = DLType.NONE;

  public DownloadInfo() {}

  public DownloadInfo(URL url, File local, String name) {
    this(url, local, name, null, "md5");
  }

  public DownloadInfo(URL url, File local, String name, List<String> hash, String hashType,
      DLType primary, DLType backup) {
    this(url, local, name, hash, hashType);
    if (primary != null) {
      this.primaryDLType = primary;
    }
    if (backup != null) {
      this.backupDLType = backup;
    }
  }

  public DownloadInfo(URL url, File local, String name, List<String> hash, String hashType) {
    this.url = url;
    this.local = local;
    this.name = name;
    this.hash = hash;
    this.hashType = hashType;
  }



  public static List<DownloadInfo> getDownloadInfo(File base, Map<String, String> map) {
    List<DownloadInfo> infoList = Lists.newArrayList();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String reqURL = null;
      reqURL = Constants.MG_GET_SCRIPT + "?data=" + entry.getValue();
      try {
        File local = new File(base, entry.getKey());
        DownloadInfo info = new DownloadInfo(new URL(reqURL), local, local.getName());
        info.setPrimaryDLType(DLType.ContentMD5);
        infoList.add(info);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return infoList;
  }

  public DLType getPrimaryDLType() {
    return primaryDLType;
  }

  public void setPrimaryDLType(DLType primaryDLType) {
    this.primaryDLType = primaryDLType;
  }

  public DLType getBackupDLType() {
    return backupDLType;
  }

  public void setBackupDLType(DLType backupDLType) {
    this.backupDLType = backupDLType;
  }

  @Override
  public String toString() {
    return String
        .format("Local File: %s, URL: %s, Name: %s", local.getPath(), url.toString(), name);
  }

  public enum DLType {
    ETag, ContentMD5, NONE
  }
}
