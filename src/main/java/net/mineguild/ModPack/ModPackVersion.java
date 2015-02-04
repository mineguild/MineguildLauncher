package net.mineguild.ModPack;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

import com.google.gson.annotations.Expose;

public class ModPackVersion implements Comparable<ModPackVersion> {

  protected @Expose @Getter @Setter String version;
  protected @Expose @Getter @Setter String forgeVersion;
  protected @Expose @Getter @Setter String minecraftVersion;
  protected @Expose @Getter @Setter String modpackName;
  protected @Expose @Getter String hash;
  protected @Expose @Getter long releaseTime;

  @Override
  public int compareTo(ModPackVersion o) {
    if (this.equals(o)) {
      return 0;
    } else if (isNewer(o)) {
      return 1;
    } else {
      return -1;
    }
  }

  public boolean isNewer(ModPackVersion otherPack) {
    return otherPack.getReleaseTime() < this.getReleaseTime();
  }

  public String getReleaseDate() {
    return new Date(releaseTime).toString();
  }
  
  @Override
  public String toString(){
    return String.format("%s (%s)", version, getReleaseDate());
  }
  
}
