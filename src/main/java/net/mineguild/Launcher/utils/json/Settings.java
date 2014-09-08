package net.mineguild.Launcher.utils.json;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.utils.CryptoUtils;

public class Settings {

  private @Getter @Setter Map<String, Object> mojangdata;
  private String clientToken;
  private @Getter @Setter String MCUser;
  private String MCPassword;
  private @Getter @Setter String modpack_hash;
  private @Getter @Setter String additional_java_args;
  private @Getter @Setter File modpackPath;

  public Settings(File modpackPath) {
    this.modpackPath = modpackPath;
    MCUser = "";
    MCPassword = "";
    modpack_hash = "";
    additional_java_args = "";
    clientToken = CryptoUtils.encrypt(UUID.randomUUID().toString());
  }

  public String getClientToken(){
    try {
      return CryptoUtils.decrypt(clientToken);
    } catch (Exception e){
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
}
