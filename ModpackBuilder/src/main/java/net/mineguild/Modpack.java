package net.mineguild;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.org.apache.xpath.internal.operations.Mod;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Modpack {
    private long build;
    private String version;
    private String hash;
    private long releaseTime;
    private Map<File, Long> modpackFiles = new HashMap<>();
    public Modpack(long build, String version, long releaseTime, HashMap<File, Long> modpackFiles){
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<File, Long> getModpackFiles() {
        return modpackFiles;
    }

    public void setModpackFiles(Map<File, Long> modpackFiles) {
        this.modpackFiles = modpackFiles;
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

    public boolean removeFile(File file){
        @SuppressWarnings("unchecked")
        HashMap<File, Long> copy = (HashMap<File, Long>) modpackFiles;
        for (File modpackFile : copy.keySet()) {
            if(file.getName().equals(file.getName())){
                modpackFiles.remove(modpackFile);
                return true;
            }
        }
        return false;
    }

    public String toJson(){
        Gson g = new GsonBuilder().setPrettyPrinting().create();
        return g.toJson(this);
    }

    public void addFile(File file, long checkSum){
        modpackFiles.put(file, checkSum);
    }


}
