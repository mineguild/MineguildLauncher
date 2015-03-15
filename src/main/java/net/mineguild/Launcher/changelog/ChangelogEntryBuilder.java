package net.mineguild.Launcher.changelog;

import net.mineguild.ModPack.ModInfo;


public class ChangelogEntryBuilder {
  
  public static ChangelogEntry create(String entry){
    return new ChangelogTextEntry(entry);
  }
  
  public static ChangelogEntry create(String file1, String file2, ChangelogFileEntry.ChangelogAction action){
    ChangelogFileEntry entry = new ChangelogFileEntry();
    entry.action = action;
    entry.file1 = file1;
    entry.file2 = file2;
    return entry;
  }
  
  public static ChangelogEntry create(ModInfo mod1, ModInfo mod2, ChangelogModEntry.ChangelogModAction action){
    ChangelogModEntry entry = new ChangelogModEntry();
    entry.action = action;
    entry.mod1 = mod1;
    entry.mod2 = mod2;
    return entry;
  }

}
