package net.mineguild.ModPack;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.mineguild.Launcher.utils.RelativePath.getRelativePath;

import java.io.File;

import lombok.Getter;
import lombok.Setter;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class ModPackFile implements Comparable<ModPackFile> {
  private @Getter @Expose String path;
  private @Getter @Setter @Expose long size;
  private @Getter @Setter @Expose String name;
  private @Getter @Setter @Expose Side side = Side.UNIVERSAL;
  private @Getter @Setter @Expose boolean optional = false;
  private @Getter @Expose String hash;
  private @Getter @Setter File localFile; // We just keep this here...

  public ModPackFile(File relative_to, File f, String hash) {
    this(getRelativePath(relative_to, f), hash);
    this.localFile = f;
    this.size = f.length();
  }

  public ModPackFile(String path, String hash) {
    checkNotNull(hash);
    checkNotNull(path);
    this.path = path;
    this.hash = hash;
  }

  public File getFile(File baseDirectory) {
    checkNotNull(baseDirectory);
    return new File(baseDirectory, path);
  }
  
  @Override
  public int compareTo(ModPackFile o) {
    return this.path.compareTo(o.path);
  }
  
  @Override
  public String toString(){
    return new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
  }
  

}
