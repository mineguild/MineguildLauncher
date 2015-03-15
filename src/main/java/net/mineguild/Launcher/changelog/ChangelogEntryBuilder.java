package net.mineguild.Launcher.changelog;

import java.io.File;

public class ChangelogEntryBuilder {
  
  public static ChangelogEntry create(String entry){
    return new ChangelogTextEntry(entry);
  }
  
  public static ChangelogEntry create(File file1, File file2, ChangelogFileEntry.ChangelogAction action){
    return null;
  }

}
