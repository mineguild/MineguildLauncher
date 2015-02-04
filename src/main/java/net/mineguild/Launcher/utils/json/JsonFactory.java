package net.mineguild.Launcher.utils.json;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FileUtils;

import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.utils.ChecksumUtil;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.json.assets.AssetIndex;
import net.mineguild.Launcher.utils.json.versions.Version;
import net.mineguild.ModPack.ModInfo;
import net.mineguild.ModPack.ModPack;
import net.mineguild.ModPack.ModpackRepository;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonFactory {
  public static final Gson GSON;

  static {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapterFactory(new EnumAdaptorFactory());
    builder.registerTypeAdapter(Date.class, new DateAdapter());
    builder.registerTypeAdapter(File.class, new FileAdapter());
    builder.registerTypeAdapter(Dimension.class, new DimensionAdapter());
    builder.registerTypeAdapter(Point.class, new PointAdapter());
    builder.enableComplexMapKeySerialization();
    builder.setPrettyPrinting();
    GSON = builder.create();
  }

  public static AssetIndex loadAssetIndex(File json) throws IOException {
    FileReader reader = new FileReader(json);
    return GSON.fromJson(reader, AssetIndex.class);
  }

  public static Version loadVersion(File json) throws IOException {
    FileReader reader = new FileReader(json);
    return GSON.fromJson(reader, Version.class);
  }

  public static Settings loadSettings(File json) throws IOException {
    FileReader reader = new FileReader(json);
    return GSON.fromJson(reader, Settings.class);
  }

  public static ModPack loadModpack(File json) throws IOException {
    FileReader reader = new FileReader(json);
    return GSON.fromJson(reader, ModPack.class);
  }

  public static List<ModInfo> loadModInfoFile(File json) throws IOException {
    FileReader reader = new FileReader(json);
    List<ModInfo> mods = Lists.newArrayList(GSON.fromJson(reader, ModInfo[].class));
    return mods;
  }

  public static List<ModInfo> loadModInfoFromJar(File f) throws IOException {
    JarFile jar = new JarFile(f);
    ZipEntry mcmod = jar.getEntry("mcmod.info");
    if (mcmod != null) {
      InputStreamReader reader = new InputStreamReader(jar.getInputStream(mcmod));
      List<ModInfo> mods = Lists.newArrayList(GSON.fromJson(reader, ModInfo[].class));
      jar.close();
      return mods;
    } else {
      Logger.logDebug(f.getPath() + " has no mcmod.info! Ignoring.");
      jar.close();
      return null;
    }

  }

  public static MCVersionIndex loadVersionIndex(File f) throws IOException {
    FileReader reader = new FileReader(f);
    return GSON.fromJson(reader, MCVersionIndex.class);
  }

  public static BuilderSettings loadBuilderSettings(File json) {
    try {
      FileReader reader = new FileReader(json);
      return GSON.fromJson(reader, BuilderSettings.class);
    } catch (IOException e) {
      return new BuilderSettings();
    }
  }

  public static ModpackRepository loadRepository(String url) throws Exception {
    URL fileURL = new URL(url);
    File repositoryFile = new File(OSUtils.getLocalDir(), ChecksumUtil.getMD5(url));
    FileUtils.copyURLToFile(fileURL, repositoryFile);
    FileReader reader = new FileReader(repositoryFile);
    return GSON.fromJson(reader, ModpackRepository.class);
  }
  
  public static ModpackRepository loadRepository(File f) throws Exception {
    FileReader reader = new FileReader(f);
    return GSON.fromJson(reader, ModpackRepository.class);
  }
}
