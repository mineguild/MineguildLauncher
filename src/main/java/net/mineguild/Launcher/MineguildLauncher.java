package net.mineguild.Launcher;

import java.io.File;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import net.mineguild.Launcher.download.DownloadDialog;
import net.mineguild.Launcher.download.DownloadInfo;
import net.mineguild.Launcher.download.DownloadTask;
import net.mineguild.Launcher.minecraft.LoginResponse;
import net.mineguild.Launcher.minecraft.MCInstaller;
import net.mineguild.Launcher.utils.ModpackUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.common.collect.Lists;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

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
    System.setProperty("java.net.preferIPv4Stack" , "true");
    baseDirectory = new File("modpack/");
    baseDirectory.mkdirs();
    DownloadTask.ssl_hack();
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
    } catch(Exception e){
      success = false;
    }
    
    if(success){
      //JOptionPane.showMessageDialog(null, "Modpack can be launched now. Well actually, it could be if it the launcher was able to.");
      String user = JOptionPane.showInputDialog("Please enter your username/email");
      String pass = JOptionPane.showInputDialog("Please enter your password");
      YggdrasilUserAuthentication authentication = (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(Proxy.NO_PROXY,"Minecraft").createUserAuthentication(Agent.MINECRAFT);
      authentication.setUsername(user);
      authentication.setPassword(pass);
      try{
        authentication.logIn();
      } catch (AuthenticationException e){
        JOptionPane.showMessageDialog(null, "Invalid credentials!");
      }
      LoginResponse res = new LoginResponse(Integer.toString(authentication.getAgent().getVersion()), "token", "nylser", null, authentication.getSelectedProfile().getId().toString(), authentication);
      MCInstaller.launchMinecraft(m, res);
    } else {
      JOptionPane.showMessageDialog(null, "Something went wrong! Modpack can't be launched now!");
    }
    
    
    
    System.exit(0);
  }
}
