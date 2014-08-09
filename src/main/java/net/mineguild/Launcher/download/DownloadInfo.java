package net.mineguild.Launcher.download;

import java.io.File;
import java.net.URL;
import java.util.List;

public class DownloadInfo {
    public URL url;
    public File local;
    public String name;
    public long size = 0;
    public List<String> hash;
    public String hashType;
    private DLType primaryDLType = DLType.ETag;

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

    private DLType backupDLType = DLType.NONE;

    public DownloadInfo() {
    }

    public DownloadInfo(URL url, File local, String name, Boolean ftbServers) {
        this(url, local, name, null, "md5");
        if (ftbServers) {
            primaryDLType = DLType.ContentMD5;
            backupDLType = DLType.FTBBackup;
        }
    }

    public DownloadInfo(URL url, File local, String name) {
        this(url, local, name, null, "md5");
    }

    public DownloadInfo(URL url, File local, String name, List<String> hash, String hashType, DLType primary, DLType backup) {
        this(url, local, name, hash, hashType);
        if (primary != null)
            this.primaryDLType = primary;
        if (backup != null)
            this.backupDLType = backup;
    }

    public DownloadInfo(URL url, File local, String name, List<String> hash, String hashType) {
        this.url = url;
        this.local = local;
        this.name = name;
        this.hash = hash;
        this.hashType = hashType;
    }

    public enum DLType {
        ETag, ContentMD5, FTBBackup, NONE
    }
}
