package net.mineguild.ModPack;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.TreeSet;

import lombok.Getter;
import lombok.Setter;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.json.JsonFactory;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.annotations.Expose;

public class ModpackRepository {
  @Expose @Getter @Setter String jsonUrl;
  @Expose @Getter @Setter String logoUrl;
  @Expose @Getter @Setter Map<String, VersionRepository> packs;

  public ModpackRepository() {
    packs = Maps.newTreeMap();
  }

  public static class VersionRepository {
    @Expose @Getter @Setter TreeSet<ModPackVersion> versions;
    @Expose @Getter @Setter String name;
    @Expose @Getter @Setter String repoBaseURL;

    public VersionRepository() {
      versions = Sets.newTreeSet();
    }

    public VersionRepository(String name) {
      this();
      this.name = name;
    }

    public ModPack loadPack(ModPackVersion v) throws IOException {
      String modpackURL = repoBaseURL + v.getHash();
      File localFile = new File(OSUtils.getLocalDir(), v.getHash());
      FileUtils.copyURLToFile(new URL(modpackURL), localFile);
      return JsonFactory.loadModpack(localFile);
    }

    @Override
    public String toString() {
      return name;
    }
  }
  
  
}
