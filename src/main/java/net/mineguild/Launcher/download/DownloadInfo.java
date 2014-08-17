package net.mineguild.Launcher.download;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DownloadInfo {
    public static final String INFO_SCRIPT = "https://mineguild.net/download/mmp/php/info.php";
    public static final String GET_SCRIPT = "https://mineguild.net/download/mmp/php/getfile.php";
    public URL url;
    public File local;
    public String name;
    public long size = 0;
    public List<String> hash;
    public String hashType;
    private DLType primaryDLType = DLType.ETag;
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

    public static long getTotalSize(Collection<String> hashes) {
        Gson g = new Gson();
        String json_hashes = g.toJson(hashes);
        //System.out.println(json_hashes);
        URL script = null;
        try {
            script = new URL(DownloadInfo.INFO_SCRIPT + "?data=" + URLEncoder.encode(json_hashes, "utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return -1l;
        }
        try {
            HttpURLConnection con = (HttpURLConnection) script.openConnection();

            con.setRequestMethod("GET");
            con.connect();
            try {
                List<String> lines = IOUtils.readLines(con.getInputStream());
                return Long.parseLong(lines.get(lines.size() - 1));
            } catch (NumberFormatException e) {
                System.out.println("Invalid file(s)!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;

    }

    public static List<DownloadInfo> getDownloadInfo(File base, Map<String, String> map) {
        List<DownloadInfo> infoList = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String reqURL = null;
            reqURL = DownloadInfo.GET_SCRIPT + "?data=" + entry.getValue();
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
        return String.format("Local File: %s, URL: %s, Name: %s", local.getPath(), url.toString(), name);
    }

    public enum DLType {
        ETag, ContentMD5, FTBBackup, NONE
    }
}
