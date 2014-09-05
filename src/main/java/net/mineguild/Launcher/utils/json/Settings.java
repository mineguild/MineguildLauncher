package net.mineguild.Launcher.utils.json;

import java.util.Map;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import net.mineguild.Launcher.utils.CryptoUtils;

public class Settings {
  
  private @Getter @Setter Map<String, Object> mojangdata;
  private @Getter String clientToken;
  private @Getter @Setter String MCUser;
  private String MCPassword;
  private @Getter @Setter String modpack_hash;
  private @Getter @Setter String additional_java_args;
  
  public Settings(){
    MCUser = "";
    MCPassword = "";
    modpack_hash = "";
    additional_java_args = "";
    clientToken = UUID.randomUUID().toString();
  }
  
  
  public String getMCPassword(){
    try {
      String pass = CryptoUtils.decrypt(MCPassword);
      return pass;
    } catch (Exception e){
      return "";
    }
  }
  
  public void setMCPassword(String password){
    MCPassword = CryptoUtils.encrypt(password);
  }
  
  public void clearPassword(){
    MCPassword = ""; 
  }
}
