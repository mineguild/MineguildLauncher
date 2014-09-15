package net.mineguild.Launcher;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import net.mineguild.Launcher.utils.ChecksumUtil;
import net.mineguild.Launcher.utils.RelativePath;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class X_Modpack {
  private @Expose @Getter @Setter String version;
  private @Expose @Getter String hash;
  private @Expose @Getter long releaseTime;
  private @Expose @Getter @Setter Map<String, String> modpackFiles = new HashMap<String, String>();
  private @Expose @Getter @Setter String forgeVersion;
  private @Expose @Getter @Setter String minecraftVersion;

  private List<File> unprocessedFiles = Lists.newArrayList(); // Local variable
  private @Getter @Setter File basePath; // Local variable -- doesn't belong to json.


  public X_Modpack(String version, long releaseTime, Map<String, String> modpackFiles) {

    this.version = version;
    this.hash = ChecksumUtil.getMD5(Long.toString(releaseTime));
    this.releaseTime = releaseTime;
    this.modpackFiles = modpackFiles;
  }

  public X_Modpack(long releaseTime) {
    this.releaseTime = releaseTime;
  }

  public X_Modpack(File basePath) {
    this.releaseTime = -1;
    this.basePath = basePath;
  }

  public static X_Modpack fromJson(String json) {
    Gson g = new Gson();
    return g.fromJson(json, X_Modpack.class);
  }

  public String toJson() {
    if (unprocessedFiles != null) {
      if (unprocessedFiles.size() > 0) {
        processFiles();
      }
    }
    Gson g = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    return g.toJson(this);
  }

  public void setReleaseTime(long releaseTime) {
    this.releaseTime = releaseTime;
    this.hash = ChecksumUtil.getMD5(Long.toString(releaseTime));
  }

  public static HashMap<String, String> getNew(X_Modpack oldPack, X_Modpack newPack) {
    HashMap<String, String> newFiles = Maps.newHashMap();
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

  public static Map<String, String> getOld(X_Modpack oldPack, X_Modpack newPack) {
    Map<String, String> oldFiles = Maps.newHashMap();
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

  public HashMap<String, String> getNew(X_Modpack newPack) {
    return X_Modpack.getNew(this, newPack);
  }

  public Map<String, String> getOld(X_Modpack newPack) {
    return X_Modpack.getOld(this, newPack);
  }


  public void addModpackFiles(Map<File, String> modpackFiles) {
    for (Map.Entry<File, String> entry : modpackFiles.entrySet()) {
      this.addFile(entry.getKey(), entry.getValue());
    }
  }

  public void addModpackFiles() {
    if (basePath != null) {
      this.addFiles(FileUtils.listFiles(new File(basePath, "mods"), Constants.MODPACK_FILE_FILTER,
          Constants.MODPACK_DIR_FILTER));
      this.addFiles(FileUtils.listFiles(new File(basePath, "config"),
          Constants.MODPACK_FILE_FILTER, Constants.MODPACK_DIR_FILTER));
    }
  }


  public boolean isNewer(X_Modpack otherPack) {
    return otherPack.getReleaseTime() <= this.getReleaseTime();
  }


  public void addFile(File file, String checkSum) {
    if (basePath != null) {
      modpackFiles.put(
          FilenameUtils.separatorsToUnix(RelativePath.getRelativePath(basePath, file)), checkSum);
    }
  }

  public void processFiles() {
    Map<File, String> result = ChecksumUtil.getChecksum(unprocessedFiles, Hashing.md5());
    for (Map.Entry<File, String> entry : result.entrySet()) {
      addFile(entry.getKey(), entry.getValue());
    }
    unprocessedFiles.clear();
  }

  public void addFile(File file) {
    unprocessedFiles.add(file);
    processFiles();
  }

  public void addFiles(Collection<File> files) {
    unprocessedFiles.addAll(files);
    processFiles();
  }

  public Collection<String> getFilesBySum(String sum) {
    Collection<String> files = Lists.newArrayList();
    for (Map.Entry<String, String> entry : modpackFiles.entrySet()) {
      if (entry.getValue().equals(sum)) {
        files.add(entry.getKey());
      }
    }
    return files;
  }

  public String getReleaseDate() {
    return new Date(releaseTime).toString();
  }


}
