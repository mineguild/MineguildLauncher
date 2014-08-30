package net.mineguild.Launcher;

import net.mineguild.Launcher.download.DownloadTask;
import net.mineguild.Launcher.utils.ModpackUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

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
import java.util.HashMap;
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

    Agent ag = new Agent("MineguildLauncher", 2);
    YggdrasilUserAuthentication authentication =
        (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(Proxy.NO_PROXY, "MG")
            .createUserAuthentication(Agent.MINECRAFT);
    authentication.loadFromStorage(g.fromJson(FileUtils.readFileToString(new File("profile.json")), Map.class));
    authentication.logIn();
    FileUtils.write(new File("profile.json"), g.toJson(authentication.saveForStorage()));
    System.out.println(g.toJson(authentication.saveForStorage()));

    System.out.println(g.toJson(authentication.getSelectedProfile()));
    authentication.getSelectedProfile();
    System.exit(0);
    baseDirectory = new File("modpack");
    baseDirectory.mkdirs();
    // args = new String[]{"old"};
    if (args.length == 1) {
      if (args[0].equals("old")) {
        FileUtils.cleanDirectory(baseDirectory);
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

    // List<File> files = (List<File>) FileUtils.listFiles(new File("testPack"),
    // FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter(".dis")),
    // FileFilterUtils.trueFileFilter());
    // long startTime = System.currentTimeMillis();
    // Map<File, String> result = ChecksumUtil.getChecksum(files, Hashing.md5());
    // long endTime = System.currentTimeMillis();
    // float difference = endTime - startTime;

    /*
     * for (Object o : result.keySet()) { String key = o.toString(); String value =
     * result.get(o).toString();
     * 
     * System.out.println(key + " " + value); } System.out.printf("Time taken: %f seconds\n",
     * difference/1000);
     */

    /*
     * for(String f : Modpack.getNew(m, newPack).keySet()){ FileUtils.deleteQuietly(new
     * File(modpack, f)); }
     */
    //
    /*
     * long totalSize = DownloadInfo.getTotalSize(neededFiles.values()); DownloadDialog d = new
     * DownloadDialog(info, "Downloading Configs&Mods [Early Beta]", totalSize); d.setVisible(true);
     * d.start(); d.dispose();
     */
    System.exit(0);
  }
}
