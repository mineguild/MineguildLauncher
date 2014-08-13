package net.mineguild.Launcher;

import com.google.common.hash.Hashing;
import net.mineguild.Launcher.download.DownloadDialog;
import net.mineguild.Launcher.download.DownloadInfo;
import net.mineguild.Launcher.download.DownloadTask;
import net.mineguild.Launcher.utils.ChecksumUtil;
import net.mineguild.Launcher.utils.DownloadUtils;
import net.mineguild.Launcher.utils.HTTPDownloadUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import javax.swing.*;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class MineguildLauncher {
    public static void main(String[] args) throws Exception{
        //DownloadDialog d = new DownloadDialog(new HashMap<String, File>(0), "Test");
        //d.setVisible(true); //STUFF...
        DownloadTask.ssl_hack();
        File modpack = new File("modpack");
        modpack.mkdirs();
        List<DownloadInfo> info;
        Map<String, String> neededFiles;
        if(args.length == 1){
            if(args[0].equals("old")){
                FileUtils.cleanDirectory(modpack);
                Modpack m = Modpack.fromJson(IOUtils.toString(new URL("https://mineguild.net/download/mmp/test_pack.json")));
                neededFiles = DownloadUtils.getNeededFiles(modpack, m.getModpackFiles(), false);
                info = DownloadInfo.getDownloadInfo(modpack, neededFiles);
            }  else {
                Modpack m = Modpack.fromJson(IOUtils.toString(new URL("https://mineguild.net/download/mmp/test_pack.json")));
                Modpack newPack = Modpack.fromJson(IOUtils.toString(new URL("https://mineguild.net/download/mmp/test_pack_new.json")));
                neededFiles = DownloadUtils.getNeededFiles(modpack, newPack.getModpackFiles(), false);
                info = (ArrayList<DownloadInfo>) DownloadInfo.getDownloadInfo(modpack, neededFiles);
                DownloadUtils.deleteUnneededFiles(modpack, newPack.getModpackFiles(), m.getOld(newPack));
            }
        }
        else {
            Modpack m = Modpack.fromJson(IOUtils.toString(new URL("https://mineguild.net/download/mmp/test_pack.json")));
            Modpack newPack = Modpack.fromJson(IOUtils.toString(new URL("https://mineguild.net/download/mmp/test_pack_new.json")));
            neededFiles = DownloadUtils.getNeededFiles(modpack, newPack.getModpackFiles(), false);
            info = (ArrayList<DownloadInfo>) DownloadInfo.getDownloadInfo(modpack, neededFiles);
            DownloadUtils.deleteUnneededFiles(modpack, newPack.getModpackFiles(), m.getOld(newPack));
        }
        DownloadTask.ssl_hack();
        try {
            UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //List<File> files = (List<File>) FileUtils.listFiles(new File("testPack"), FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter(".dis")), FileFilterUtils.trueFileFilter());
        //long startTime = System.currentTimeMillis();
        //Map<File, String> result = ChecksumUtil.getChecksum(files, Hashing.md5());
        //long endTime = System.currentTimeMillis();
        //float difference = endTime - startTime;

        /*
        for (Object o : result.keySet()) {
            String key = o.toString();
            String value = result.get(o).toString();

            System.out.println(key + " " + value);
        }
        System.out.printf("Time taken: %f seconds\n", difference/1000);*/

        /*for(String f : Modpack.getNew(m, newPack).keySet()){
            FileUtils.deleteQuietly(new File(modpack, f));
        }*/
        //
        long totalSize = DownloadInfo.getTotalSize(neededFiles.values());
        DownloadDialog d = new DownloadDialog(info, "Downloading Configs&Mods [Early Beta]", totalSize);
        d.setVisible(true);
        d.start();
        d.dispose();

        System.exit(0);
    }
}