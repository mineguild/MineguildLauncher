package net.mineguild.Launcher;

import net.mineguild.Launcher.utils.DownloadDialog;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

public class MineguildLauncher {
    public static void main(String[] args){
        DownloadDialog d = new DownloadDialog(new HashMap<String, File>(0), "Test");
        d.setVisible(true); //STUFF...
    }
}