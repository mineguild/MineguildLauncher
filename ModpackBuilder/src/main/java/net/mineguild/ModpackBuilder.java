package net.mineguild;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ModpackBuilder {

    public static File modpackDirectory;

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        getModpackDirectory();
        Modpack newPack = new Modpack(modpackDirectory);
        File modpack_json = new File("newer_test.json");
        Modpack oldPack = Modpack.fromJson(FileUtils.readFileToString(new File("new_test.json")));
        newPack.addModpackFiles();
        Gson g = new GsonBuilder().setPrettyPrinting().create();
        FileUtils.write(modpack_json, newPack.toJson());
        System.out.println(g.toJson(oldPack.getNew(newPack)));
        //fromUploadFiles(newPack.getModpackFiles());
        placeUploadFiles(modpackDirectory.getAbsolutePath(), oldPack.getNew(newPack));
        //System.out.println(g.toJson(Modpack.getOld(oldPack, newPack)));
        /*
        Modpack m = new Modpack();
        m.setReleaseTime(System.currentTimeMillis());
        List<File> list = (List<File>) FileUtils.listFiles(new File("testPack"), FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter(".dis")), FileFilterUtils.trueFileFilter());
        m.addModpackFiles(ChecksumUtil.getChecksum(list));
        FileUtils.write(new File("new_test.json"), m.toJson(), false);*/

    }


    public static void placeUploadFiles(String basePath, Map<String, String> files) {
        File uploadDir = new File("upload");
        uploadDir.mkdir();
        try {
            FileUtils.cleanDirectory(uploadDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Map.Entry<String, String> entry : files.entrySet()) {
            File file = new File(basePath, entry.getKey());
            File newDirectory = new File(uploadDir, entry.getValue().substring(0, 2));
            File newFile = new File(newDirectory, entry.getValue());
            if (file.exists()) {
                try {
                    FileUtils.copyFile(file, newFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void fromUploadFiles(Map<String, String> files) {
        File modpack = new File("modpack");
        File upload = new File("upload");
        modpack.mkdir();
        try {
            FileUtils.cleanDirectory(modpack);
        } catch (IOException ignored) {
        }
        for (Map.Entry<String, String> entry : files.entrySet()) {
            String hash = entry.getValue();
            File fileDir = new File(upload, hash.substring(0, 2));
            File file = new File(fileDir, hash);
            String path = entry.getKey();
            File filePath = new File(modpack, path);
            try {
                System.out.printf("Copying %s to %s\n", file.toString(), filePath.toString());
                FileUtils.copyFile(file, filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void getModpackDirectory() {
        JFileChooser fileChooser = new JFileChooser(new File("."));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Modpack_Json", "json", "mmp");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //fileChooser.setFileFilter(filter);
        fileChooser.setDialogTitle("Select the directory of the modpack you want to update to.");
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            if ((new File(selected, "config")).exists() && new File(selected, "config")
                .isDirectory() && (new File(selected, "mods")).exists() && (new File(selected,
                "mods")).isDirectory()) {
                modpackDirectory = selected;
            } else {
                JOptionPane.showMessageDialog(null,
                    "Invalid directory selected, please select a one containing mods and config folder.",
                    "Invalid directory!", JOptionPane.ERROR_MESSAGE);
                getModpackDirectory();
            }
        } else {
            System.exit(0);
        }
    }
}
