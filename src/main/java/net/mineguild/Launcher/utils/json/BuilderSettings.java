package net.mineguild.Launcher.utils.json;

import java.awt.Dimension;
import java.io.File;

import lombok.Getter;
import lombok.Setter;

import com.google.gson.annotations.Expose;

public class BuilderSettings {

  private @Expose @Getter @Setter String lastPath;
  private @Expose @Getter @Setter String permGen;
  private @Expose @Getter @Setter String launchPath;
  private @Expose @Getter @Setter String gameDir;
  private @Expose @Getter @Setter Dimension lastSize;
  private @Expose @Getter @Setter String javaPath;
  private @Expose @Getter @Setter long mem;

  public BuilderSettings() {
    lastPath = new File("").getAbsolutePath();
  }


}
