package net.mineguild.ModPack;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

import com.google.gson.annotations.Expose;

public class Mod extends ModPackFile {

  private @Expose @Getter @Setter ModInfo info;
  private @Expose @Getter @Setter String curseforgeID;
  private @Expose @Getter @Setter Date curseforgeUploaded;

  public Mod(String hash, long size) {
    super(hash, size);
  }

  public static Mod fromModPackFile(ModPackFile f) {
    Mod m = new Mod(f.getHash(), f.getSize());
    m.setSide(f.getSide());
    m.setOptional(f.isOptional());
    m.setSHA256(f.getSHA256());
    return m;
  }


}
