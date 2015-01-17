package net.mineguild.Launcher.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import lombok.Getter;
import lombok.Setter;
import net.mineguild.ModPack.Mod;
import net.mineguild.ModPack.ModPack;
import net.mineguild.ModPack.ModPackFile;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CurseforgeUtil {

    public static final String BASE = "http://minecraft.curseforge.com/";
    public static final String MCMODS = BASE + "mc-mods/";

    public static Map<String, TreeSet<CurseforgeFile>> getVersions(String modid)
        throws IOException {
        Map<String, TreeSet<CurseforgeFile>> versions = Maps.newTreeMap();
        Document d = getCachedDocument(MCMODS + modid + "/files");
        Elements items = d.getElementsByClass("project-file-list-item");
        for (Element item : items) {
            String filename = item.getElementsByClass("project-file-name-container").get(0)
                .getElementsByClass("overflow-tip").get(0).ownText();
            String mcVersion = item.getElementsByClass("version-label").get(0).ownText();
            String dllink = item.getElementsByClass("project-file-download-button").get(0)
                .getElementsByClass("e-button").get(0).attr("href");
            Date uploaded = new Date(Long.parseLong(
                item.getElementsByClass("project-file-date-uploaded").select("abbr").first()
                    .attr("data-epoch")) * 1000);
            CurseforgeFile file = new CurseforgeFile();
            file.setDlLink(BASE + dllink);
            file.setFilename(filename);
            file.setMcVersion(mcVersion);
            file.setUploaded(uploaded);
            if (!versions.containsKey(mcVersion)) {
                versions.put(mcVersion, new TreeSet<CurseforgeUtil.CurseforgeFile>());
            }
            versions.get(mcVersion).add(file);
        }
        return versions;
    }

    public static void checkForUpdate(ModPack oldPack, ModPack workPack) {
        List<Mod> common = getCommonFiles(oldPack, workPack);
        System.out.println(common.size());
        for (Mod mod : common) {
            if (mod.getCurseforgeID() != null) {
                if (!mod.getCurseforgeID().isEmpty()) {
                    Gson g = new GsonBuilder().setPrettyPrinting().create();
                    try {
                        System.out
                            .println(g.toJson(getVersions(mod.getCurseforgeID()).get("1.7.10")));
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static List<Mod> getCommonFiles(ModPack oldPack, ModPack workPack) {
        List<Mod> common = Lists.newArrayList();
    /*for(String hash : workPack.getFiles().keySet()){
      Set<ModPackFile> hashMatching = (Set) oldPack.getFilesByHash(hash).entrySet();
      if(hashMatching.size() > 0){
        for(ModPackFile file : hashMatching){
          if(file instanceof Mod){
            common.add((Mod) file);
          }
        }
      }
    }*/
        for (ModPackFile f : oldPack.getFiles().values()) {
            if (f instanceof Mod) {
                common.add((Mod) f);
            }
        }
        return common;
    }

    public static Document getCachedDocument(String requestPath) throws IOException {
        File cache =
            new File(new File(OSUtils.getLocalDir(), "cache"), ChecksumUtil.getMD5(requestPath));
        if (cache.exists()) {
            if (System.currentTimeMillis() - cache.lastModified() > 60000) {
                cache.delete();
            }
        }
        cache.getParentFile().mkdirs();
        if (!cache.exists()) {
            URL requestURL = new URL(requestPath);
            FileUtils.copyURLToFile(requestURL, cache);
        }
        return Jsoup.parse(cache, "utf-8");
    }

    public static class CurseforgeFile implements Comparable<CurseforgeFile> {
        private @Getter @Setter Date uploaded;
        private @Getter @Setter String filename;
        private @Getter @Setter String mcVersion;
        private @Getter @Setter String nominalSize;
        private @Getter @Setter String dlLink;

        @Override public int compareTo(CurseforgeFile o) {
            return uploaded.compareTo(o.getUploaded());
        }
    }

}
