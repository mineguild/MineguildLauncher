package net.mineguild.ModPack;

import java.util.List;

import com.google.gson.annotations.Expose;

import lombok.Getter;

public class ModInfo {
    private @Expose @Getter String modid;
    private @Expose @Getter String name;
    private @Expose @Getter String version;
    private @Expose @Getter String mcversion;
    private @Expose @Getter String description;
    private @Expose @Getter String credits;
    private @Expose @Getter String logoFile;
    private @Expose @Getter String url;
    private @Expose @Getter String updateUrl;
    private @Expose @Getter List<String> authorList;
    private @Expose @Getter String parent;
    private @Expose @Getter List<String> dependencies;

}
