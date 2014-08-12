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
        Modpack m = Modpack.fromJson(IOUtils.toString(new URL("https://mineguild.net/download/mmp/test_pack.json")));
        /*
        for (Object o : result.keySet()) {
            String key = o.toString();
            String value = result.get(o).toString();

            System.out.println(key + " " + value);
        }
        System.out.printf("Time taken: %f seconds\n", difference/1000);*/
        DownloadInfo.getTotalSize(m.getModpackFiles().values());
        ArrayList<DownloadInfo> info = (ArrayList<DownloadInfo>) DownloadInfo.getDownloadInfo(new File("modpack"), m.getModpackFiles());
        File modpack = new File("modpack");
        modpack.mkdirs();
        FileUtils.cleanDirectory(modpack);
        long totalSize = DownloadInfo.getTotalSize(m.getModpackFiles().values());
        DownloadDialog d = new DownloadDialog(info, "Downloading Configs&Mods [Early Beta]", totalSize);
        d.setVisible(true);
        d.start();
        d.dispose();

        System.exit(0);
    }
}