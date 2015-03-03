package net.mineguild.Launcher.utils.json;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import net.mineguild.ModPack.ModPack;
import net.mineguild.ModPack.ModpackRepository;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonWriter {

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
    builder.excludeFieldsWithoutExposeAnnotation();
    GSON = builder.create();
  }

  public static void saveSettings(Settings set, File json) throws IOException {
    FileUtils.write(json, GSON.toJson(set, Settings.class));
  }

  public static void saveBuilderSettings(BuilderSettings set, File json) throws IOException {
    FileUtils.write(json, GSON.toJson(set, BuilderSettings.class));
  }

  public static void saveModpack(ModPack pack, File json) throws IOException {
    FileUtils.write(json, GSON.toJson(pack, ModPack.class));
  }
  
  public static void saveModpack(ModPack pack, OutputStream os) throws IOException {
    IOUtils.write(GSON.toJson(pack, ModPack.class), os);
  }

  public static void saveRepository(ModpackRepository repo, File json) throws IOException {
    GsonBuilder builder = new GsonBuilder();
    builder.enableComplexMapKeySerialization();
    builder.setPrettyPrinting();
    builder.excludeFieldsWithoutExposeAnnotation();
    builder.setExclusionStrategies(new ModpackExclusionStrategy(null));
    Gson g2 = builder.create();
    FileUtils.write(json, g2.toJson(repo, ModpackRepository.class));
  }
  
  public static void saveRepository(ModpackRepository repo, OutputStream os) throws IOException {
    GsonBuilder builder = new GsonBuilder();
    builder.enableComplexMapKeySerialization();
    builder.setPrettyPrinting();
    builder.excludeFieldsWithoutExposeAnnotation();
    builder.setExclusionStrategies(new ModpackExclusionStrategy(null));
    Gson g2 = builder.create();
    IOUtils.write(g2.toJson(repo, ModpackRepository.class), os);
  }

}
