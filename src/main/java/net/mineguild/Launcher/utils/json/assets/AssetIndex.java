package net.mineguild.Launcher.utils.json.assets;

import java.util.Map;

public class AssetIndex {
    public Map<String, Asset> objects;
    public boolean virtual;
    public boolean modpack;

    public static class Asset {
        public String hash;
        public long size;
    }
}