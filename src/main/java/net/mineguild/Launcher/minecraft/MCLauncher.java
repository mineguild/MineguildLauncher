package net.mineguild.Launcher.minecraft;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

import net.mineguild.Launcher.MineguildLauncher;
import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.utils.ChecksumUtil;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.Parallel;
import net.mineguild.Launcher.utils.json.JsonFactory;
import net.mineguild.Launcher.utils.json.OldPropertyMapSerializer;
import net.mineguild.Launcher.utils.json.Settings.JavaSettings;
import net.mineguild.Launcher.utils.json.assets.AssetIndex;
import net.mineguild.Launcher.utils.json.assets.AssetIndex.Asset;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.UserType;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.util.UUIDTypeAdapter;

public class MCLauncher {

  public static boolean isLegacy = false;
  private static StringBuilder cpb;

  public static Process launchMinecraft(String javaPath, String gameFolder, File assetDir,
      File nativesDir, List<File> classpath, String mainClass, String args, String assetIndex,
      String version, UserAuthentication authentication, boolean legacy, JavaSettings settings)
      throws IOException {

    cpb = new StringBuilder("");
    isLegacy = legacy;
    File gameDir = new File(gameFolder);
    assetDir = syncAssets(assetDir, assetIndex);

    for (File f : classpath) {
      cpb.append(OSUtils.getJavaDelimiter());
      cpb.append(f.getAbsolutePath());
    }

    List<String> arguments = Lists.newArrayList();

    Logger.logInfo("Java Path: " + javaPath);
    Logger.logInfo("MC Version: " + version);
    arguments.add(javaPath);

    setMemory(arguments, settings);

    if (OSUtils.getCurrentOS().equals(OSUtils.OS.WINDOWS)) {
      if (!OSUtils.is64BitWindows()) {
        if (settings.getPermGen() == null || settings.getPermGen().isEmpty()) {
          if (OSUtils.getOSTotalMemory() > 2046) {
            settings.setPermGen("192m");
            Logger.logInfo("Defaulting PermSize to 192m");
          } else {
            settings.setPermGen("192m");
            Logger.logInfo("Defaulting PermSize to 128m");
          }
        }
      }
    }

    if (settings.getPermGen() == null || settings.getPermGen().isEmpty()) {
      // 64-bit or Non-Windows
      settings.setPermGen("256m");
      Logger.logInfo("Defaulting PermSize to 256m");
    }

    arguments.add("-XX:PermSize=" + settings.getPermGen());
    arguments.add("-Djava.library.path=" + nativesDir.getAbsolutePath());
    arguments.add("-Dorg.lwjgl.librarypath=" + nativesDir.getAbsolutePath());
    arguments.add("-Dnet.java.games.input.librarypath=" + nativesDir.getAbsolutePath());
    arguments.add("-Duser.home=" + gameDir.getParentFile().getAbsolutePath());

    // Use IPv4 when possible, only use IPv6 when connecting to IPv6 only addresses
    arguments.add("-Djava.net.preferIPv4Stack=true");

    /*
     * if (Settings.getSettings().getUseSystemProxy()) {
     * arguments.add("-Djava.net.useSystemProxies=true"); }
     */

    arguments.add("-cp");
    arguments.add(cpb.toString());

    /*
     * String additionalOptions = Settings.getSettings().getAdditionalJavaOptions(); if
     * (!additionalOptions.isEmpty()) { Logger.logInfo("Additional java parameters: " +
     * additionalOptions); for (String s : additionalOptions.split("\\s+")) { if
     * (s.equalsIgnoreCase("-Dfml.ignoreInvalidMinecraftCertificates=true") && !isLegacy) {
     * ErrorUtils.tossError("JARMODDING DETECTED in 1.6.4+ " + s,
     * "FTB Does not support jarmodding in MC 1.6+ "); } else { arguments.add(s); } } }
     */
    // if (Settings.getSettings().getOptJavaArgs()) {
    if (settings.isOptimizationArgumentsUsed()) {
      Logger.logInfo("Adding Optimization Arguments");
      Collections.addAll(arguments,
          "-XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CICompilerCountPerCPU -XX:+TieredCompilation"
              .split("\\s+"));
    }

    // Undocumented environment variable to control JVM
    String additionalEnvVar = System.getenv("_JAVA_OPTIONS");
    if (additionalEnvVar != null && !additionalEnvVar.isEmpty()) {
      Logger.logInfo("_JAVA_OPTIONS defined: " + additionalEnvVar);
    }
    // Documented environment variable to control JVM
    additionalEnvVar = System.getenv("JAVA_TOOL_OPTIONS");
    if (additionalEnvVar != null && !additionalEnvVar.isEmpty()) {
      Logger.logInfo("JAVA_TOOL_OPTIONS defined: " + additionalEnvVar);
    }

    arguments.add(mainClass);
    for (String s : args.split(" ")) {
      boolean done = false;
      if (authentication.getSelectedProfile() != null) {
        if (s.equals("${auth_player_name}")) {
          arguments.add(authentication.getSelectedProfile().getName());
          done = true;
        } else if (s.equals("${auth_uuid}")) {
          arguments.add(UUIDTypeAdapter.fromUUID(authentication.getSelectedProfile().getId()));
          done = true;
        } else if (s.equals("${user_type}")) {
          arguments.add(authentication.getUserType().getName());
          done = true;
        }
      } else {
        if (s.equals("${auth_player_name}")) {
          arguments.add("Player");
          done = true;
        } else if (s.equals("${auth_uuid}")) {
          arguments.add(new UUID(0L, 0L).toString());
          done = true;
        } else if (s.equals("${user_type}")) {
          arguments.add(UserType.LEGACY.getName());
          done = true;
        }
      }
      if (!done) {
        if (s.equals("${auth_session}")) {
          if (authentication.isLoggedIn() && authentication.canPlayOnline()) {
            if (authentication instanceof YggdrasilUserAuthentication && !isLegacy) {
              arguments.add(String.format("token:%s:%s", authentication.getAuthenticatedToken(),
                  UUIDTypeAdapter.fromUUID(authentication.getSelectedProfile().getId())));
            } else {
              arguments.add(authentication.getAuthenticatedToken());
            }
          } else {
            arguments.add("-");
          }
        } else if (s.equals("${auth_access_token}"))
          arguments.add(authentication.getAuthenticatedToken());
        else if (s.equals("${version_name}"))
          arguments.add(version);
        else if (s.equals("${game_directory}"))
          arguments.add(gameDir.getAbsolutePath());
        else if (s.equals("${game_assets}") || s.equals("${assets_root}"))
          arguments.add(assetDir.getAbsolutePath());
        else if (s.equals("${assets_index_name}"))
          arguments.add(assetIndex == null ? "legacy" : assetIndex);
        else if (s.equals("${user_properties}"))
          arguments.add(new GsonBuilder()
              .registerTypeAdapter(PropertyMap.class, new OldPropertyMapSerializer()).create()
              .toJson(authentication.getUserProperties()));
        else if (s.equals("${user_properties_map}"))
          arguments.add(new GsonBuilder()
              .registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create()
              .toJson(authentication.getUserProperties()));
        else
          arguments.add(s);
      }
    }/*
      * if (!isLegacy) {// legacy is handled separately boolean fullscreen = false; if
      * (Settings.getSettings().getLastExtendedState() == JFrame.MAXIMIZED_BOTH) {
      * GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment(); Rectangle
      * bounds = env.getMaximumWindowBounds(); arguments.add("--width");
      * arguments.add(String.valueOf((int) bounds.getWidth())); arguments.add("--height");
      * arguments.add(String.valueOf((int) bounds.getHeight())); fullscreen = true; } Dimension def
      * = new Dimension(854, 480); if (Settings.getSettings().getLastDimension().getWidth() !=
      * def.getWidth() && !fullscreen) { arguments.add("--width");
      * arguments.add(String.valueOf((int) Settings.getSettings().getLastDimension().getWidth())); }
      * if (Settings.getSettings().getLastDimension().getHeight() != def.getHeight() && !fullscreen)
      * { arguments.add("--height"); arguments.add(String.valueOf((int)
      * Settings.getSettings().getLastDimension().getHeight())); } }
      */


    ProcessBuilder builder = new ProcessBuilder(arguments);
    /*
     * StringBuilder tmp = new StringBuilder(); for (String a : builder.command())
     * tmp.append(a).append(' '); Logger.logInfo("Launching: " + tmp.toString());
     */
    builder.directory(gameDir);
    builder.redirectErrorStream(true);
    // OSUtils.cleanEnvVars(builder.environment());
    return builder.start();
  }

  private static void setMemory(List<String> arguments, JavaSettings settings) {
    boolean memorySet = false;
    try {
      int min = 256;
      if (settings.getMaxMemory() > 0) {
        arguments.add("-Xms" + min + "M");
        Logger.logInfo("Setting MinMemory to " + min);
        arguments.add("-Xmx" + settings.getMaxMemory() + "M");
        Logger.logInfo("Setting MaxMemory to " + settings.getMaxMemory());
        memorySet = true;
      }
    } catch (Exception e) {
      Logger.logError("Error parsing memory settings", e);
    }
    if (!memorySet) {
      arguments.add("-Xms" + 256 + "M");
      Logger.logInfo("Defaulting MinMemory to " + 256);
      arguments.add("-Xmx" + 1024 + "M");
      Logger.logInfo("Defaulting MaxMemory to " + 1024);
      settings.setMaxMemory(1024);
    }
  }

  private static File syncAssets(File assetDir, String indexName) throws JsonSyntaxException,
      JsonIOException, IOException {
    Logger.logInfo("Syncing Assets:");
    final File objects = new File(assetDir, "objects");
    AssetIndex index =
        JsonFactory.loadAssetIndex(new File(assetDir, "indexes/{INDEX}.json".replace("{INDEX}",
            indexName)));

    if (!index.virtual)
      return assetDir;

    final File targetDir = new File(assetDir, "virtual/" + indexName);

    final ConcurrentSkipListSet<File> old = new ConcurrentSkipListSet<File>();
    old.addAll(FileUtils.listFiles(targetDir, FileFilterUtils.trueFileFilter(),
        FileFilterUtils.trueFileFilter()));

    // Benchmark.reset("threading");
    Parallel.TaskHandler<Void> th =
        new Parallel.ForEach<Entry<String, Asset>, Void>(index.objects.entrySet())
            .withFixedThreads(2 * OSUtils.getNumCores())
            // .configurePoolSize(2*2*OSUtils.getNumCores(), 10)
            .apply(new Parallel.F<Entry<String, Asset>, Void>() {
              public Void apply(Entry<String, Asset> e) {
                Asset asset = e.getValue();
                File local = new File(targetDir, e.getKey());
                File object = new File(objects, asset.hash.substring(0, 2) + "/" + asset.hash);

                old.remove(local);

                try {
                  if (local.exists() && !ChecksumUtil.getSHA(local).equals(asset.hash)) {
                    Logger.logInfo("  Changed: " + e.getKey());
                    FileUtils.copyFile(object, local);
                  } else if (!local.exists()) {
                    Logger.logInfo("  Added: " + e.getKey());
                    FileUtils.copyFile(object, local);
                  }
                } catch (Exception ex) {
                  Logger.logError("Asset checking failed: ", ex);
                }
                return null;
              }
            });
    try {
      th.shutdown();
      th.wait(60, TimeUnit.SECONDS);
    } catch (Exception ex) {
      Logger.logError("Asset checking failed: ", ex);
    }
    // Benchmark.logBenchAs("threading", "parallel asset(virtual) check");

    for (File f : old) {
      f.getAbsolutePath().replace(targetDir.getAbsolutePath(), "");
      // Logger.logInfo("  Removed: " + name.substring(1));
      f.delete();
    }

    return targetDir;
  }

  public static void killMC() {
    if (MineguildLauncher.MCRunning && MineguildLauncher.procmon != null) {
      MineguildLauncher.procmon.stop();
      Logger.logWarn("Minecraft was killed by user.");
    }
  }


}
