package net.mineguild.Launcher;

import com.google.common.collect.BiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.mineguild.Launcher.utils.ChecksumUtil;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.*;

public class Modpack {
    private long build;
    private String version;
    private String hash;
    private long releaseTime;
    private Map<String, String> modpackFiles = new HashMap<>();

    public Modpack(long build, String version, long releaseTime, Map<String, String> modpackFiles) {
        this.build = build;
        this.version = version;
        this.hash = ChecksumUtil.getMD5(version);
        this.releaseTime = releaseTime;
        this.modpackFiles = modpackFiles;
    }

    public Modpack(long build){
        this.build = build;
    }

    public Modpack(){
        this.build = -1;
    }

    public static Modpack fromJson(String json){
        Gson g = new Gson();
        return g.fromJson(json, Modpack.class);
    }

    public static HashMap<String, String> getNew(Modpack oldPack, Modpack newPack) {
        HashMap<String, String> newFiles = new HashMap<>();
        for (Map.Entry<String, String> newEntry : newPack.getModpackFiles().entrySet()) {
            if (oldPack.getModpackFiles().containsKey(newEntry.getKey())) {
                if (!oldPack.getModpackFiles().get(newEntry.getKey()).equals(newEntry.getValue())) {
                    newFiles.put(newEntry.getKey(), newEntry.getValue());
                }
            } else {
                newFiles.put(newEntry.getKey(), newEntry.getValue());
            }
        }
        return newFiles;
    }

    public static List<String> getOld(Modpack oldPack, Modpack newPack) {
        List<String> oldFiles = new ArrayList<>();
        for (Map.Entry<String, String> oldEntry : oldPack.getModpackFiles().entrySet()) {
            if (newPack.getModpackFiles().containsKey(oldEntry.getKey())) {
                if (!newPack.getModpackFiles().get(oldEntry.getKey()).equals(oldEntry.getValue())) {
                    oldFiles.add(oldEntry.getKey());
                }
            } else {
                oldFiles.add(oldEntry.getKey());
            }
        }
        return oldFiles;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getModpackFiles() {
        return modpackFiles;
    }

    public void setModpackFiles(BiMap<String, String> modpackFiles) {
        this.modpackFiles = modpackFiles;
    }

    public void addModpackFiles(Map<File, String> modpackFiles) {
        for (Map.Entry<File, String> entry : modpackFiles.entrySet()) {
            this.addFile(entry.getKey(), entry.getValue());
        }
    }

    public long getReleaseTime() {

        return releaseTime;
    }

    public void setReleaseTime(long releaseTime) {
        this.releaseTime = releaseTime;
    }

    public long getBuild() {
        return build;
    }

    public void setBuild(long build) {
        this.build = build;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String toJson() {
        Gson g = new GsonBuilder().setPrettyPrinting().create();
        return g.toJson(this);
    }

    public void addFile(File file, String checkSum) {

        String path = FilenameUtils.separatorsToUnix(file.getPath());
        String relPath = "";
        String[] split = path.split("/");
        boolean hitPath = false;
        for (String s : split) {
            if (hitPath) {
                relPath += "/" + s;
            } else if (s.equals("config") || s.equals("mods")) {
                relPath += s;
                hitPath = true;
            }
        }
        try {
            modpackFiles.put(relPath, checkSum);
        } catch (IllegalArgumentException e) {
            System.out.println("File already added!");
        }

    }

    public Collection<String> getFilesBySum(String sum) {
        Collection<String> files = new ArrayList<>();
        for (Map.Entry<String, String> entry : modpackFiles.entrySet()) {
            if (entry.getValue().equals(sum)) {
                files.add(entry.getKey());
            }
        }
        return files;
    }


}
