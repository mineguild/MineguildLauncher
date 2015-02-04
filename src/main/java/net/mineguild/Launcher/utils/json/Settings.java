package net.mineguild.Launcher.utils.json;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;

import lombok.Getter;
import lombok.Setter;
import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.minecraft.MCInstaller;
import net.mineguild.Launcher.utils.CryptoUtils;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.ModPack.ModpackRepository;

public class Settings {

    private @Expose @Getter @Setter Map<String, Object> profile;
    private @Expose String clientToken;
    private @Expose @Getter @Setter String MCUser;
    private @Expose String MCPassword;
    private @Expose @Getter @Setter String modpack_hash;
    private @Expose File modpackPath;
    private @Expose @Getter File instancePath;
    private @Expose @Getter @Setter File minecraftResourcePath;
    private @Expose @Getter @Setter Dimension lastSize;
    private @Expose @Getter @Setter Point lastLocation;
    private @Expose @Getter @Setter boolean autoLogin;
    private @Expose @Getter @Setter BuilderSettings builderSettings;
    private @Expose @Getter @Setter JavaSettings javaSettings;
    private @Expose @Getter @Setter boolean redStyle = true;
    private @Expose @Getter @Setter long consoleBufferSize = 500;
    private @Expose @Getter @Setter int downloadThreads = OSUtils.getNumCores();
    private @Expose @Getter @Setter boolean facebookAsked = false;
    private @Expose @Getter @Setter List<String> repositories;
    private @Expose @Getter @Setter String lastPack;
    private @Expose @Getter @Setter File instancesPath;

    public Settings() {
        minecraftResourcePath = new File(OSUtils.getLocalDir(), "modpack");
        //instancePath = new File(minecraftResourcePath, "minecraft");
        instancesPath = new File(minecraftResourcePath, "packs");
        lastLocation = null;
        MCUser = "";
        MCPassword = "";
        // consoleBufferSize = 500;
        lastSize = null;
        clientToken = CryptoUtils.encrypt(UUID.randomUUID().toString());
        builderSettings = new BuilderSettings();
        javaSettings = new JavaSettings();
        repositories = Lists.newArrayList();
    }

    public String getClientToken() {
        try {
            return CryptoUtils.decrypt(clientToken);
        } catch (Exception e) {
            Logger.logError("Found invalid/migrated clientToken... Generating new Token", e);
            clientToken = CryptoUtils.encrypt(UUID.randomUUID().toString());
            return getClientToken();
        }
    }

    public String getMCPassword() {
        try {
            String pass = CryptoUtils.decrypt(MCPassword);
            return pass;
        } catch (Exception e) {
            return "";
        }
    }

    public void setMCPassword(String password) {
        MCPassword = CryptoUtils.encrypt(password);
    }

    public void clearPassword() {
        MCPassword = "";
    }

    public static class JavaSettings {
        private @Expose @Getter @Setter String javaPath = MCInstaller.getDefaultJavaPath();
        private @Expose @Getter @Setter String additionalArguments = "";
        private @Expose @Getter @Setter boolean optimizationArgumentsUsed = true;
        private @Expose @Getter @Setter int maxMemory = 2048;
        private @Expose @Getter @Setter String permGen = "";

        public JavaSettings() {
        }

    }

}
