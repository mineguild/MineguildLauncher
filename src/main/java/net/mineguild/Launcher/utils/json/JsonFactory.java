package net.mineguild.Launcher.utils.json;

import java.awt.Dimension;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import net.mineguild.Launcher.utils.json.assets.AssetIndex;
import net.mineguild.Launcher.utils.json.versions.Version;
import net.mineguild.ModPack.ModPack;

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

}
