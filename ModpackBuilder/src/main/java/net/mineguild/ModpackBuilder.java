package net.mineguild;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModpackBuilder {
    public static void main(String[] args)throws Exception{

        Modpack m = new Modpack(1);
        m.setReleaseTime(System.currentTimeMillis());
        List<File> list = (List<File>) FileUtils.listFiles(new File("testPack/"), FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter(".dis")), FileFilterUtils.trueFileFilter());
        m.setModpackFiles(ChecksumUtil.getChecksum(list));
        FileUtils.write(new File("test.json"), m.toJson(), false);

    }
}
