package net.mineguild.Launcher.utils.json;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import net.mineguild.Launcher.ModPack;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonWriter {

  public static final Gson GSON;

  static {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapterFactory(new EnumAdaptorFactory());
    builder.registerTypeAdapter(Date.class, new DateAdapter());
    builder.registerTypeAdapter(File.class, new FileAdapter());
    builder.enableComplexMapKeySerialization();
    builder.setPrettyPrinting();
    builder.excludeFieldsWithoutExposeAnnotation();
    GSON = builder.create();
  }

  public static void saveSettings(Settings set, File json) throws IOException {
    FileUtils.write(json, GSON.toJson(set, Settings.class));
  }
  
  public static void saveModpack(ModPack pack, File json) throws IOException {
    FileUtils.write(json, GSON.toJson(pack, ModPack.class));
  }

}
