package net.mineguild.Launcher.changelog;

import net.mineguild.ModPack.ModInfo;

public class ChangelogModEntry extends ChangelogEntry {

  public ModInfo mod1;
  public ModInfo mod2;
  public ChangelogModAction action;

  @Override
  public String getText() {
    switch (action) {
      case REMOVE:
        return String.format("%s(%s) was removed.", mod1.getName(), mod1.getVersion());
      case ADD:
        return String.format("%s(%s) was added.", mod1.getName(), mod1.getVersion());
      case VERSION_CHANGE:
        return String.format("Mod %s version changed from %s to %s.", mod1.getName(),
            mod1.getVersion(), mod2.getVersion());
      default:
        return null;
    }
  }

  public static enum ChangelogModAction {
    REMOVE, ADD, VERSION_CHANGE
  }

}
