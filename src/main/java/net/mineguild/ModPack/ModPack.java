package net.mineguild.ModPack;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;
import net.mineguild.Launcher.utils.ChecksumUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.annotations.Expose;

public class ModPack {
    private @Expose @Getter @Setter String version;
    private @Expose @Getter @Setter String forgeVersion;
    private @Expose @Getter @Setter String minecraftVersion;
    private @Expose @Getter String hash;
    private @Expose @Getter long releaseTime;
    private @Expose @Getter Map<String, Mod> mods;
    private @Expose @Getter Map<String, ModPackFile> other;
    private Map<String, ModPackFile> files;

    public ModPack(String version, long releaseTime) {
        this.version = version;
        this.hash = ChecksumUtil.getMD5(Long.toString(releaseTime));
        this.releaseTime = releaseTime;
    }

    public ModPack(long releaseTime) {
        this.releaseTime = releaseTime;
    }

    public ModPack() {
        mods = Maps.newTreeMap();
        other = Maps.newTreeMap();
    }

    public void setReleaseTime(long releaseTime) {
        this.releaseTime = releaseTime;
        this.hash = ChecksumUtil.getMD5(Long.toString(releaseTime));
    }


    public boolean isNewer(ModPack otherPack) {
        return otherPack.getReleaseTime() < this.getReleaseTime();
    }

    public String getReleaseDate() {
        return new Date(releaseTime).toString();
    }


    public ModPackFile getFileByPath(String path) {
        return getFiles().get(path);
    }

    public Map<String, ModPackFile> getFiles() {
        if (files != null) {
            if (!files.isEmpty()) {
                other.putAll(files);
                files.clear();
            }
        }
        Map<String, ModPackFile> allFiles = Maps.newTreeMap();
        allFiles.putAll(other);
        allFiles.putAll(mods);
        return allFiles;
    }

    public Map<String, ModPackFile> getFilesByHash(String hash) {
        Map<String, ModPackFile> ret = Maps.newTreeMap();
        for (Map.Entry<String, ModPackFile> entry : getFiles().entrySet()) {
            if (entry.getValue().getHash().equals(hash)) {
                ret.put(entry.getKey(), entry.getValue());
            }
        }
        return ret;
    }

    public Map<String, ModPackFile> getFilesByHashAndSide(String hash, Side side) {
        Map<String, ModPackFile> ret = Maps.newTreeMap();
        for (Map.Entry<String, ModPackFile> entry : getFiles().entrySet()) {
            if (entry.getValue().getHash().equals(hash)) {
                if (entry.getValue().getSide() == Side.UNIVERSAL
                    || entry.getValue().getSide() == side) {
                    ret.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return ret;
    }

    public List<String> getTopLevelDirectories() {
        List<String> ret = Lists.newArrayList();
        for (String path : getFiles().keySet()) {
            String directory = path.split("/")[0];
            if (!ret.contains(directory)) {
                ret.add(directory);
            }
        }
        return ret;
    }

    public Map<String, ModPackFile> getFilesMatching(String regex) {
        Map<String, ModPackFile> ret = Maps.newTreeMap();
        Pattern p = Pattern.compile(regex);
        for (Map.Entry<String, ModPackFile> entry : getFiles().entrySet()) {
            Matcher m = p.matcher(entry.getKey());
            if (m.find()) {
                ret.put(entry.getKey(), entry.getValue());
            }
        }
        return ret;
    }

    public void setFiles(Map<String, ModPackFile> map) {
        for (Map.Entry<String, ModPackFile> entry : map.entrySet()) {
            if (entry.getValue() instanceof Mod) {
                mods.put(entry.getKey(), (Mod) entry.getValue());
            } else {
                other.put(entry.getKey(), entry.getValue());
            }
        }
    }

}
