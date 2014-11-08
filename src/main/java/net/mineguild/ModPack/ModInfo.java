package net.mineguild.ModPack;

import java.util.List;

import lombok.Getter;

public class ModInfo {
  private @Getter String modid;
  private @Getter String name;
  private @Getter String mcversion;
  private @Getter String description;
  private @Getter String credits;
  private @Getter String logoFile;
  private @Getter String url;
  private @Getter String updateUrl;
  private @Getter List<String> authorList;
  private @Getter String parent;
  private @Getter List<String> dependencies;
  
}
