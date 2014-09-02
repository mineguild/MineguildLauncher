package net.mineguild.Launcher.utils.json;

import lombok.Getter;
import lombok.Setter;
import net.mineguild.Launcher.utils.CryptoUtils;

public class Settings {
  
  private @Getter @Setter String MCToken;
  private @Getter @Setter String MCUser;
  private String MCPassword;
  private @Getter @Setter String modpack_hash;
  private @Getter @Setter String additional_java_args;
  
  
  public String getMCPassword(){
    return CryptoUtils.decrypt(MCPassword);
  }
  
  public void setMCPassword(String password){
    MCPassword = CryptoUtils.encrypt(password);
  }
}
