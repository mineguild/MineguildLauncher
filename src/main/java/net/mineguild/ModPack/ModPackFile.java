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


  /**
   * Creates a new instance of {@link ModPackFile} with the given size and hash.
   * 
   * @param hash The file's MD5 hash (hexdigest)
   * @param size The file's size in bytes
   */
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

  @Override
  public String toString() {
    return String.format("ModPackFile Hash: %s Size: %.2d", hash, size);
  }


}
