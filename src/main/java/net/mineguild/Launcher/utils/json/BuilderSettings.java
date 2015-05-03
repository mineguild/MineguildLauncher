package net.mineguild.Launcher.utils.json;

import java.awt.Dimension;
import java.io.File;
import java.util.List;

import net.mineguild.Launcher.utils.CryptoUtils;
import net.mineguild.Launcher.utils.json.Settings.JavaSettings;
import lombok.Getter;
import lombok.Setter;

import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;

public class BuilderSettings {

  private @Expose @Getter @Setter String lastPath;
  private @Expose @Getter @Setter String launchPath;
  private @Expose @Getter @Setter String gameDir;
  private @Expose @Getter @Setter Dimension lastSize;
  private @Expose @Getter @Setter JavaSettings javaSettings;
  private @Expose @Getter List<UploadSettings> uploadSettings = Lists.newArrayList();

  public BuilderSettings() {
    lastPath = new File("").getAbsolutePath();
    lastSize = new Dimension();
    javaSettings = new JavaSettings();
  }


  public static class UploadSettings {
    private @Expose @Getter @Setter String name;
    private @Expose @Getter @Setter String address;
    private @Expose @Getter @Setter String repoPath;
    private @Expose @Getter @Setter String versionsPath;
    private @Expose @Getter @Setter String filePath;
    private @Expose @Getter @Setter String username;
    private @Expose String password;

    public String getPassword() {
      try {
        String pass = CryptoUtils.decrypt(password);
        return pass;
      } catch (Exception e) {
        return "";
      }
    }

    public void setPassword(String password) {
      password = CryptoUtils.encrypt(password);
    }

    public void clearPassword() {
      password = "";
    }

    @Override
    public String toString() {
      return name + String.format("<%s>", address);
    }
  }


}
