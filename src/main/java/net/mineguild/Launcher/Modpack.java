package net.mineguild.Launcher;

import com.google.common.collect.BiMap;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import net.mineguild.Launcher.utils.ChecksumUtil;
import net.mineguild.Launcher.utils.RelativePath;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.File;
import java.util.*;

public class Modpack {
    private
    @Getter
    @Setter
    String version;
    private String hash;
    private
    @Getter
    @Setter
    long releaseTime;
    private List<File> unprocessedFiles = new ArrayList<>();
    private Map<String, String> modpackFiles = new HashMap<>();
    private @Getter @Setter File basePath;


    public Modpack(String version, long releaseTime, Map<String, String> modpackFiles) {

        this.version = version;
        this.hash = ChecksumUtil.getMD5(version);
        this.releaseTime = releaseTime;
        this.modpackFiles = modpackFiles;
    }

    public Modpack(long releaseTime) {
        this.releaseTime = releaseTime;
    }

    public Modpack(File basePath) {
        this.releaseTime = -1;
        this.basePath = basePath;
    }

    public static Modpack fromJson(String json) {
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

    public static Map<String, String> getOld(Modpack oldPack, Modpack newPack) {
        Map<String, String> oldFiles = new HashMap<>();
        for (Map.Entry<String, String> oldEntry : oldPack.getModpackFiles().entrySet()) {
            if (newPack.getModpackFiles().containsKey(oldEntry.getKey())) {
                if (!newPack.getModpackFiles().get(oldEntry.getKey()).equals(oldEntry.getValue())) {
                    oldFiles.put(oldEntry.getKey(), oldEntry.getValue());
                }
            } else {
                oldFiles.put(oldEntry.getKey(), oldEntry.getValue());
            }
        }
        return oldFiles;
    }

    public HashMap<String, String> getNew(Modpack newPack) {
        return Modpack.getNew(this, newPack);
    }

    public Map<String, String> getOld(Modpack newPack) {
        return Modpack.getOld(this, newPack);
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

    public void addModpackFiles(){
        this.addFiles(FileUtils.listFiles(basePath, FileFilterUtils.and(FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter(".dis")), FileFilterUtils.sizeFileFilter(1l, true)), FileFilterUtils.trueFileFilter()));
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean isNewer(Modpack otherPack) {
        return otherPack.getReleaseTime() >= this.getReleaseTime();
    }

    public String toJson() {
        if(unprocessedFiles.size() > 0){
            processFiles();
        }
        Gson g = new GsonBuilder().setPrettyPrinting().create();
        return g.toJson(this);
    }

    public void addFile(File file, String checkSum) {
        modpackFiles.put(FilenameUtils.separatorsToUnix(RelativePath.getRelativePath(basePath, file)), checkSum);
    }

    public void processFiles(){
        Map<File, String> result = ChecksumUtil.getChecksum(unprocessedFiles, Hashing.md5());
        for(Map.Entry<File, String> entry : result.entrySet()){
            addFile(entry.getKey(), entry.getValue());
        }
        unprocessedFiles.clear();
    }

    public void addFile(File file){
        unprocessedFiles.add(file);
        processFiles();
    }

    public void addFiles(Collection<File> files){
        unprocessedFiles.addAll(files);
        processFiles();
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
