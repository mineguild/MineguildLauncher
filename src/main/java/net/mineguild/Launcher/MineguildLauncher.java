package net.mineguild.Launcher;

import net.mineguild.Launcher.utils.ChecksumUtil;
import net.mineguild.Launcher.utils.DownloadDialog;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.*;

public class MineguildLauncher {
    public static void main(String[] args){
        //DownloadDialog d = new DownloadDialog(new HashMap<String, File>(0), "Test");
        //d.setVisible(true); //STUFF...
        List<File> files = (List<File>) FileUtils.listFiles(new File("mods"), FileFilterUtils.fileFileFilter(), FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter("*.dis")));
        long startTime = System.currentTimeMillis();
        Map result = ChecksumUtil.getChecksum(files);
        long endTime = System.currentTimeMillis();
        float difference = endTime - startTime;
        for (Object o : result.keySet()) {
            String key = o.toString();
            String value = result.get(o).toString();

            System.out.println(key + " " + value);
        }
        System.out.printf("Time taken: %f seconds\n", difference/1000);
    }
}