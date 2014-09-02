package net.mineguild.Launcher;

import java.awt.Color;
import java.io.File;
import java.net.Proxy;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.mineguild.Launcher.log.Console;
import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.minecraft.LoginDialog;
import net.mineguild.Launcher.minecraft.LoginResponse;
import net.mineguild.Launcher.minecraft.MCInstaller;
import net.mineguild.Launcher.minecraft.ProcessMonitor;
import net.mineguild.Launcher.utils.DownloadUtils;
import net.mineguild.Launcher.utils.ModpackUtils;
import net.mineguild.Launcher.utils.json.Settings;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.sun.corba.se.impl.activation.ProcessMonitorThread;
import com.sun.xml.internal.ws.api.server.DocumentAddressResolver;

public class MineguildLauncher {

  public static File baseDirectory;
  public static boolean doExactCheck;
  public static boolean MCRunning;
  public static ProcessMonitor procmon;
  public static Console con;
  public static Settings settings;

  public static void main(String[] args) throws Exception {
    // DownloadDialog d = new DownloadDialog(new HashMap<String, File>(0), "Test");
    // d.setVisible(true); //STUFF...
    try {
      // UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
      // UIManager.setLookAndFeel(
      // UIManager.getSystemLookAndFeelClassName());
      UIManager.put("nimbusBase", Color.BLACK);
      UIManager.put("text", Color.WHITE);
      UIManager.put("nimbusLightBackground", Color.DARK_GRAY);
      UIManager.put("control", Color.DARK_GRAY);
      for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    DownloadUtils.ssl_hack();
    System.setProperty("java.net.preferIPv4Stack", "true");
    con = new Console();
    con.setVisible(true);
    LoginDialog dialog = new LoginDialog(con);
    dialog.run();
    LoginResponse res = dialog.response;
    baseDirectory = new File("modpack/");
    baseDirectory.mkdirs();

    Logger.addListener(con);
    Logger.logInfo("Test");
    Modpack m;
    if (args.length == 1) {
      if (args[0].equals("old")) {
        FileUtils.cleanDirectory(new File(ModpackUtils.getGameDir(), "mods"));
        FileUtils.cleanDirectory(new File(ModpackUtils.getGameDir(), "config"));
        m =
            Modpack.fromJson(IOUtils.toString(new URL(
                "https://mineguild.net/download/mmp/test_pack.json")));
        ModpackUtils.updateModpack(m);

      } else {
        m =
            Modpack.fromJson(IOUtils.toString(new URL(
                "https://mineguild.net/download/mmp/test_pack.json")));
        Modpack newPack =
            Modpack.fromJson(IOUtils.toString(new URL(
                "https://mineguild.net/download/mmp/test_pack_new.json")));
        ModpackUtils.updateModpack(m, newPack);
        m = newPack;
      }
    } else {
      m =
          Modpack.fromJson(IOUtils.toString(new URL(
              "https://mineguild.net/download/mmp/test_pack.json")));
      Modpack newPack =
          Modpack.fromJson(IOUtils.toString(new URL(
              "https://mineguild.net/download/mmp/test_pack_new.json")));
      ModpackUtils.updateModpack(m, newPack);
      m = newPack;
    }
    boolean success = true;
    try {
      MCInstaller.setup(m);
    } catch (Exception e) {
      success = false;
    }

    if (success) {
      MCInstaller.launchMinecraft(m, res);
    } else {
      JOptionPane.showMessageDialog(null, "Something went wrong! Modpack can't be launched now!");
    }
  }
}
