package net.mineguild.Launcher;

import net.mineguild.Launcher.download.DownloadDialog;
import net.mineguild.Launcher.download.DownloadInfo;
import net.mineguild.Launcher.download.DownloadTask;
import net.mineguild.Launcher.utils.ModpackUtils;
import net.mineguild.Launcher.utils.json.JSONFactory;
import net.mineguild.Launcher.utils.json.assets.AssetIndex;
import net.mineguild.Launcher.utils.json.assets.AssetIndex.Asset;
import net.mineguild.Launcher.utils.json.versions.Library;
import net.mineguild.Launcher.utils.json.versions.Version;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.reflect.Reflection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.Agent;
import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.BaseUserAuthentication;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.HttpUserAuthentication;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

import javax.swing.*;

import java.io.File;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MineguildLauncher {

  public static File baseDirectory;
  public static boolean doExactCheck;

  public static void main(String[] args) throws Exception {
    // DownloadDialog d = new DownloadDialog(new HashMap<String, File>(0), "Test");
    // d.setVisible(true); //STUFF...
    try {
      UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
    } catch (Exception e) {
      e.printStackTrace();
    }
    DownloadTask.ssl_hack();
    Gson g = new GsonBuilder().setPrettyPrinting().create();

    /*
     * Agent ag = new Agent("MineguildLauncher", 2); YggdrasilUserAuthentication authentication =
     * (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(Proxy.NO_PROXY, "MG")
     * .createUserAuthentication(Agent.MINECRAFT);
     * authentication.loadFromStorage(g.fromJson(FileUtils.readFileToString(new //
     * File("profile.json")), Map.class)); authentication.logIn(); FileUtils.write(new
     * File("profile.json"), g.toJson(authentication.saveForStorage()));
     * System.out.println(g.toJson(authentication.saveForStorage()));
     * 
     * System.out.println(g.toJson(authentication.getSelectedProfile()));
     * authentication.getSelectedProfile(); System.exit(0);
     */
    
    baseDirectory = new File("modpack");
    baseDirectory.mkdirs();

    File json = new File(baseDirectory, "assets/indexes/legacy.json");
    File install_profile = new File("version.json");
    FileUtils.write(install_profile,
        IOUtils.toString(new URL(Constants.MG_FORGE + "1.7.10-10.13.0.1180" + "/version.json")));
    Version v = JSONFactory.loadVersion(install_profile);
    File mc_version =
        new File(baseDirectory, "versions/${version}/${version}".replace("${version}", v.assets));
    FileUtils.write(
        mc_version,
        IOUtils.toString(new URL(Constants.MC_DL
            + "versions/${version}/${version}.json".replace("${version}", v.assets))));
    Version mcV = JSONFactory.loadVersion(mc_version);
    List<DownloadInfo> libs = new ArrayList<DownloadInfo>();
    for (Library lib : v.getLibraries()) {
      File local = new File(baseDirectory, "libraries/" + lib.getPath());
      if (lib.checksums != null) {
        libs.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local, local.getName(),
            lib.checksums, "sha1"));
      } else {
        libs.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local, local.getName()));
      }
    }
    for (Library lib : mcV.libraries){
      if(lib.natives == null){
        File local = new File(baseDirectory, "libraries/" + lib.getPath());
        libs.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local, local.getName()));
      } else {
        File local = new File(baseDirectory, "libraries/" + lib.getPathNatives());
        if (!local.exists()) {
            libs.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPathNatives()), local, local.getName()));
        }
      }
    }
    DownloadDialog di = new DownloadDialog(libs, "Downloading Libraries");
    di.setVisible(true);
    di.start();
    di.dispose();
    if (!json.exists()) {
      FileUtils.write(json, IOUtils.toString(new URL(
          "https://s3.amazonaws.com/Minecraft.Download/indexes/${version}.json".replace(
              "${version}", v.assets))));
    }
    AssetIndex index = JSONFactory.loadAssetIndex(json);
    long totalSize = 0;
    List<DownloadInfo> info = new ArrayList<>();
    for (Map.Entry<String, Asset> a : index.objects.entrySet()) {
      totalSize += a.getValue().size;
      String path = a.getValue().hash.substring(0, 2) + "/" + a.getValue().hash;
      // System.out.println(a.getKey() + " : " + a.getValue().size);
      info.add(new DownloadInfo(new URL(Constants.MC_RES + path), new File(baseDirectory,
          "assets/objects/" + path), a.getKey(), Lists.newArrayList(a.getValue().hash), "sha1"));
    }
    di = new DownloadDialog(info, "Downloading Assets", totalSize);
    di.setVisible(true);
    di.start();
    di.dispose();

    // args = new String[]{"old"};
    if (args.length == 1) {
      if (args[0].equals("old")) {
        FileUtils.cleanDirectory(new File(baseDirectory, "mods"));
        FileUtils.cleanDirectory(new File(baseDirectory, "config"));
        Modpack m =
            Modpack.fromJson(IOUtils.toString(new URL(
                "https://mineguild.net/download/mmp/test_pack.json")));
        ModpackUtils.updateModpack(m);

      } else {
        Modpack m =
            Modpack.fromJson(IOUtils.toString(new URL(
                "https://mineguild.net/download/mmp/test_pack.json")));
        Modpack newPack =
            Modpack.fromJson(IOUtils.toString(new URL(
                "https://mineguild.net/download/mmp/test_pack_new.json")));
        ModpackUtils.updateModpack(m, newPack);
      }
    } else {
      Modpack m =
          Modpack.fromJson(IOUtils.toString(new URL(
              "https://mineguild.net/download/mmp/test_pack.json")));
      Modpack newPack =
          Modpack.fromJson(IOUtils.toString(new URL(
              "https://mineguild.net/download/mmp/test_pack_new.json")));
      ModpackUtils.updateModpack(m, newPack);
    }
    DownloadTask.ssl_hack();
    
    System.exit(0);
  }
}
