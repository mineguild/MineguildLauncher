package net.mineguild.ModPack;

import java.util.Date;

import com.google.gson.annotations.Expose;

import lombok.Getter;
import lombok.Setter;

public class Mod extends ModPackFile {

  private @Expose @Getter @Setter ModInfo info;
  private @Expose @Getter @Setter String curseforgeID;
  private @Expose @Getter @Setter Date curseforgeUploaded;

  public Mod(String hash, long size) {
    super(hash, size);
  }


}
