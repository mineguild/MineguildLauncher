package net.mineguild.ModPack;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import net.mineguild.Launcher.utils.ChecksumUtil;

import com.google.common.collect.Maps;
import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;

public class ModPack {
  private @Expose @Getter @Setter String version;
  private @Expose @Getter @Setter String forgeVersion;
  private @Expose @Getter @Setter String minecraftVersion;
  private @Expose @Getter String hash;
  private @Expose @Getter long releaseTime;
  private @Expose @Getter Map<String, ModPackFile> files;

  public ModPack(String version, long releaseTime, Map<String, ModPackFile> files) {
    this.version = version;
    this.hash = ChecksumUtil.getMD5(Long.toString(releaseTime));
    this.releaseTime = releaseTime;
    setFiles(files);
  }

  public ModPack(long releaseTime) {
    this.releaseTime = releaseTime;
  }

  public ModPack() {}

  public void setReleaseTime(long releaseTime) {
    this.releaseTime = releaseTime;
    this.hash = ChecksumUtil.getMD5(Long.toString(releaseTime));
  }

  public void setFiles(Map<String, ModPackFile> files) {
    if (!(files instanceof TreeMap<?, ?>)) {
      files = Maps.newTreeMap();
    }
    this.files = files;
  }

  public boolean isNewer(ModPack otherPack) {
    return otherPack.getReleaseTime() < this.getReleaseTime();
  }

  public String getReleaseDate() {
    return new Date(releaseTime).toString();
  }


  public ModPackFile getFileByPath(String path) {
    return files.get(path);
  }

  public Map<String, ModPackFile> getFilesByHash(String hash) {
    Map<String, ModPackFile> ret = Maps.newTreeMap();
    for (Map.Entry<String, ModPackFile> entry : files.entrySet()) {
      if (entry.getValue().getHash().equals(hash)) {
        ret.put(entry.getKey(), entry.getValue());
      }
    }
    return ret;
  }

  public Map<String, ModPackFile> getFilesByHashAndSide(String hash, Side side) {
    Map<String, ModPackFile> ret = Maps.newTreeMap();
    for (Map.Entry<String, ModPackFile> entry : files.entrySet()) {
      if (entry.getValue().getHash().equals(hash)) {
        if (entry.getValue().getSide() == Side.UNIVERSAL || entry.getValue().getSide() == side) {
          ret.put(entry.getKey(), entry.getValue());
        }
      }
    }
    return ret;
  }
  
  public List<String> getTopLevelDirectories() {
    List<String> ret = Lists.newArrayList();
    for(String path : files.keySet()) {
      String directory = path.split("/")[0];
      if(!ret.contains(directory)) {
        ret.add(directory);
      }
    }
    return ret;
  }

}
