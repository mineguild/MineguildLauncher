package net.mineguild.Launcher.utils.json;

import java.io.File;

import lombok.Getter;
import lombok.Setter;

import com.google.gson.annotations.Expose;

public class BuilderSettings {

  private @Expose @Getter @Setter String lastPath;

  public BuilderSettings() {
    lastPath = new File("").getAbsolutePath();
  }


}
