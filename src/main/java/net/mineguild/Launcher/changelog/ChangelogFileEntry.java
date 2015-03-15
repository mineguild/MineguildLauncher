package net.mineguild.Launcher.changelog;

import net.mineguild.Launcher.utils.ChecksumUtil.ModPackEntry;

public class ChangelogFileEntry extends ChangelogEntry {
  public ModPackEntry file1;
  public ModPackEntry file2;
  public ChangelogAction action;
  
  public static enum ChangelogAction {
    REMOVE, MODIFY, ADD, REPLACE
  }


  @Override
  public String getText() {
    switch(action){
      case REMOVE:
        return String.format("%s was removed.", file1.getKey());
      case MODIFY:
        return String.format("%s was modified.", file1.getKey());
      case ADD:
        return String.format("%s was added.", file1.getKey());
      case REPLACE:
        return String.format("%s(%s) was replaced by %s(%s).", file1.getKey(), file1.getValue().getHash(), file2.getKey(), file2.getValue().getHash());
      default:
        return null;
    }
  }
  
  
}
