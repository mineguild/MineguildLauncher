package net.mineguild.Launcher;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import lombok.Getter;
import lombok.Setter;
import net.mineguild.Launcher.utils.ChecksumUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.annotations.Expose;

public class ModPack {
  private @Expose @Getter @Setter String version;
  private @Expose @Getter @Setter String forgeVersion;
  private @Expose @Getter @Setter String minecraftVersion;
  private @Expose @Getter String hash;
  private @Expose @Getter long releaseTime;
  private @Expose @Getter Set<ModPackFile> files = Sets.newTreeSet();

  public ModPack(String version, long releaseTime, Set<ModPackFile> files) {
    this.version = version;
    this.hash = ChecksumUtil.getMD5(Long.toString(releaseTime));
    this.releaseTime = releaseTime;
    setFiles(files);
  }

  public ModPack(long releaseTime) {
    this.releaseTime = releaseTime;
  }

  public void setReleaseTime(long releaseTime) {
    this.releaseTime = releaseTime;
    this.hash = ChecksumUtil.getMD5(Long.toString(releaseTime));
  }

  public void setFiles(Set<ModPackFile> files) {
    if (!(files instanceof TreeSet<?>)) {
      files = Sets.newTreeSet(files);
    }
    this.files = files;
  }

  public boolean isNewer(ModPack otherPack) {
    return otherPack.getReleaseTime() <= this.getReleaseTime();
  }

  public String getReleaseDate() {
    return new Date(releaseTime).toString();
  }

  public void addFile(ModPackFile f) {
    files.add(f);
  }

  public ModPackFile getFileByPath(String path) {
    for (ModPackFile f : files) {
      if (f.getPath().equals(path)) {
        return f;
      }
    }
    return null;
  }

  public List<ModPackFile> getFilesByHash(String hash) {
    List<ModPackFile> ret = Lists.newArrayList();
    for (ModPackFile f : files) {
      if (f.getHash().equals(hash)) {
        ret.add(f);
      }
    }
    return ret;
  }

}
