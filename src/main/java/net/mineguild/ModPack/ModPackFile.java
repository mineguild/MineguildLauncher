package net.mineguild.ModPack;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;

import lombok.Getter;
import lombok.Setter;
import net.mineguild.Launcher.utils.ChecksumUtil;

import com.google.gson.annotations.Expose;


public class ModPackFile {

  @Expose @Getter @Setter long size;
  @Expose @Getter @Setter Side side = Side.UNIVERSAL;
  @Expose @Getter @Setter boolean optional = false;
  @Expose @Getter @Setter String hash;
  @Expose @Getter @Setter String SHA256;

  public ModPackFile(String hash, long size) {
    this(hash);
    this.size = checkNotNull(size);
  }

  /**
   * Creates a new instance of {@link ModPackFile} with the given hash.
   *
   * @param hash The file's hash/checksum (hexdigest)
   */
  public ModPackFile(String hash) {
    this.hash = checkNotNull(hash);
  }

  public ModPackFile() {}

  public boolean sideMatches(Side matchSide) {
    return matchSide == Side.BOTH || side == Side.UNIVERSAL || side == matchSide;
  }

  public boolean hashMatches(File localFile) throws IOException {
    return hash.equals(ChecksumUtil.getMD5(localFile));
  }
}
