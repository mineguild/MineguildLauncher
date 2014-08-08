package net.mineguild;

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
        newPack.addModpackFiles(ChecksumUtil.getChecksum(list));
        Gson g = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(g.toJson(Modpack.getNew(oldPack, newPack)));
        placeUploadFiles(new File("testPack").getAbsolutePath(), Modpack.getNew(oldPack, newPack));
        //System.out.println(g.toJson(Modpack.getOld(oldPack, newPack)));
        /*
        Modpack m = new Modpack();
        m.setReleaseTime(System.currentTimeMillis());
        List<File> list = (List<File>) FileUtils.listFiles(new File("testPack"), FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter(".dis")), FileFilterUtils.trueFileFilter());
        m.addModpackFiles(ChecksumUtil.getChecksum(list));
        FileUtils.write(new File("new_test.json"), m.toJson(), false);*/

    }


    public static void placeUploadFiles(String basePath, Map<String, Long> files) {
        File uploadDir = new File("upload");
        uploadDir.mkdir();
        try {
            FileUtils.cleanDirectory(uploadDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Map.Entry<String, Long> entry : files.entrySet()) {
            File file = new File(basePath, entry.getKey());
            File newFile = new File(uploadDir, entry.getValue().toString());
            if (file.exists()) {
                try {
                    FileUtils.copyFile(file, newFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
