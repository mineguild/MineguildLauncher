package net.mineguild;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ModpackBuilder {

    public static void main(String[] args) throws Exception {
        Modpack oldPack = Modpack.fromJson(FileUtils.readFileToString(new File("test.json")));
        Modpack newPack = new Modpack();
        List<File> list = (List<File>) FileUtils.listFiles(new File("testPack"), FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter(".dis")), FileFilterUtils.trueFileFilter());
        newPack.addModpackFiles(ChecksumUtil.getChecksum(list, Hashing.md5()));
        Gson g = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(g.toJson(Modpack.getNew(oldPack, newPack)));
        fromUploadFiles(newPack.getModpackFiles());
        //placeUploadFiles(new File("testPack").getAbsolutePath(), Modpack.getNew(oldPack, newPack));
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
        } catch (IOException ignored) {}
        for(Map.Entry<String, String> entry : files.entrySet()){
            String hash = entry.getValue();
            File fileDir = new File(upload, hash.substring(0, 2));
            File file = new File(fileDir, hash);
            String path = entry.getKey();
            File filePath = new File(modpack, path);
            try {
                System.out.printf("Copying %s to %s\n", file.toString(), filePath.toString());
                FileUtils.copyFile(file, filePath);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
