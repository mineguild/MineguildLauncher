package net.mineguild;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Modpack {
    private long build;
    private String version;
    private String hash;
    private long releaseTime;
    private Map<String, Long> modpackFiles = new HashMap<>();

    public Modpack(long build, String version, long releaseTime, Map<String, Long> modpackFiles) {
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

    public static BiMap<String, Long> getNew(Modpack oldPack, Modpack newPack) {
        BiMap<String, Long> newFiles = HashBiMap.create();
        for (Map.Entry<String, Long> newEntry : newPack.getModpackFiles().entrySet()) {
            if (oldPack.getModpackFiles().containsKey(newEntry.getKey())) {
                if (!((long) oldPack.getModpackFiles().get(newEntry.getKey()) == newEntry.getValue())) {
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
        for (Map.Entry<String, Long> oldEntry : oldPack.getModpackFiles().entrySet()) {
            if (newPack.getModpackFiles().containsKey(oldEntry.getKey())) {
                if (!((long) newPack.getModpackFiles().get(oldEntry.getKey()) == oldEntry.getValue())) {
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

    public Map<String, Long> getModpackFiles() {
        return modpackFiles;
    }

    public void setModpackFiles(BiMap<String, Long> modpackFiles) {
        this.modpackFiles = modpackFiles;
    }

    public void addModpackFiles(Map<File, Long> modpackFiles) {
        for (Map.Entry<File, Long> entry : modpackFiles.entrySet()) {
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

    public void addFile(File file, long checkSum){
        if (checkSum != 1) {
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
    }

    public String getFileBySum(Long sum) {
        return ((BiMap<String, Long>) modpackFiles).inverse().get(sum);
    }


}
