package net.mineguild.Launcher.utils.json;

import java.awt.Dimension;
import java.io.File;

import net.mineguild.Launcher.utils.json.Settings.JavaSettings;
import lombok.Getter;
import lombok.Setter;

import com.google.gson.annotations.Expose;

public class BuilderSettings {

    private @Expose @Getter @Setter String lastPath;
    private @Expose @Getter @Setter String launchPath;
    private @Expose @Getter @Setter String gameDir;
    private @Expose @Getter @Setter Dimension lastSize;
    private @Expose @Getter @Setter JavaSettings javaSettings;

    public BuilderSettings() {
        lastPath = new File("").getAbsolutePath();
        lastSize = new Dimension();
        javaSettings = new JavaSettings();
    }


}
