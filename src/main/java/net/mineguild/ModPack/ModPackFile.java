package net.mineguild.ModPack;

import static com.google.common.base.Preconditions.checkNotNull;
import lombok.Getter;
import lombok.Setter;

import com.google.gson.annotations.Expose;


public class ModPackFile {

  @Expose @Getter @Setter long size;
  @Expose @Getter @Setter Side side;
  @Expose @Getter @Setter boolean optional;
  @Expose @Getter @Setter String hash;
  
  public ModPackFile(String hash, long size) {
    this(hash);
    this.size = checkNotNull(size);
  }

  /**
   * Creates a new instance of {@link ModPackFile} with the given hash.
   * 
   * @param hash The file's MD5 hash (hexdigest)
   */
  public ModPackFile(String hash) {
    this.hash = checkNotNull(hash);
  }
  
  public ModPackFile(){
    
  }
}