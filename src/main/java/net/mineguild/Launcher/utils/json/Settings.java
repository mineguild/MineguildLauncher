package net.mineguild.Launcher.utils.json;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.util.Map;
import java.util.UUID;

import com.google.gson.annotations.Expose;

import lombok.Getter;
import lombok.Setter;
import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.minecraft.MCInstaller;
import net.mineguild.Launcher.utils.CryptoUtils;
import net.mineguild.Launcher.utils.OSUtils;

public class Settings {

  private @Expose @Getter @Setter Map<String, Object> profile;
  private @Expose String clientToken;
  private @Expose @Getter @Setter String MCUser;
  private @Expose String MCPassword;
  private @Expose @Getter @Setter String modpack_hash;
  private @Expose File modpackPath;
  private @Expose @Getter @Setter File instancePath;
  private @Expose @Getter @Setter File launchPath;
  private @Expose @Getter @Setter Dimension lastSize;
  private @Expose @Getter @Setter Point lastLocation;
  private @Expose @Getter @Setter boolean autoLogin;
  private @Expose @Getter @Setter BuilderSettings builderSettings;
  private @Expose @Getter @Setter JavaSettings javaSettings;
  private @Expose @Getter @Setter boolean redStyle = true;
  private @Expose @Getter @Setter long consoleBufferSize = 500;

  public Settings() {
    launchPath = new File(OSUtils.getLocalDir(), "modpack");
    instancePath = new File(launchPath, "minecraft");
    lastLocation = null;
    MCUser = "";
    MCPassword = "";
    //consoleBufferSize = 500;
    lastSize = null;
    clientToken = CryptoUtils.encrypt(UUID.randomUUID().toString());
    builderSettings = new BuilderSettings();
    javaSettings = new JavaSettings();
  }

  public String getClientToken() {
    try {
      return CryptoUtils.decrypt(clientToken);
    } catch (Exception e) {
      Logger.logError("Found invalid/migrated clientToken... Generating new Token", e);
      clientToken = CryptoUtils.encrypt(UUID.randomUUID().toString());
      return getClientToken();
    }
  }

  public String getMCPassword() {
    try {
      String pass = CryptoUtils.decrypt(MCPassword);
      return pass;
    } catch (Exception e) {
      return "";
    }
  }

  public void setMCPassword(String password) {
    MCPassword = CryptoUtils.encrypt(password);
  }

  public void clearPassword() {
    MCPassword = "";
  }

  public static class JavaSettings {
    private @Expose @Getter @Setter String javaPath = MCInstaller.getDefaultJavaPath();
    private @Expose @Getter @Setter String additionalArguments = "";
    private @Expose @Getter @Setter boolean optimizationArgumentsUsed = true;
    private @Expose @Getter @Setter int maxMemory = 2048;
    private @Expose @Getter @Setter String permGen = "";

    public JavaSettings() {}

  }

}
