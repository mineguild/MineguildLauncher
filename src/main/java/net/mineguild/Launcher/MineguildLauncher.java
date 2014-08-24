package net.mineguild.Launcher;

import net.mineguild.Launcher.download.DownloadTask;
import net.mineguild.Launcher.utils.ModpackUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.io.File;
import java.net.URL;

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
