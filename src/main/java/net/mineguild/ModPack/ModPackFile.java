package net.mineguild.ModPack;

import static com.google.common.base.Preconditions.checkNotNull;
import lombok.Getter;
import lombok.Setter;

import com.google.gson.annotations.Expose;

public class ModPackFile {
  private @Getter @Setter @Expose long size;
  private @Getter @Setter @Expose Side side = Side.UNIVERSAL;
  private @Getter @Setter @Expose boolean optional = false;
  private @Getter @Expose String hash;

  public ModPackFile(String hash, long size) {
    this(hash);
    this.size = size; 
  }

  public ModPackFile(String hash) {
    checkNotNull(hash);
    this.hash = hash;
  }
  
  @Override
  public String toString(){
    return String.format("ModPackFile Hash: %s Size: %.2d", hash, size);
  }
  

}
