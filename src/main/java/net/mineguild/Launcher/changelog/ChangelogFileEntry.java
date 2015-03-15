package net.mineguild.Launcher.changelog;

public class ChangelogFileEntry implements ChangelogEntry {
  public String file1;
  public String file2;
  public ChangelogAction action;
  
  public static enum ChangelogAction {
    REMOVE, MODIFY, ADD, REPLACE
  }


  @Override
  public String getText() {
    switch(action){
      case REMOVE:
        return String.format("%s was removed.", file1);
      case MODIFY:
        return String.format("%s was modified.", file1);
      case ADD:
        return String.format("%s was added.", file1);
      case REPLACE:
        return String.format("%s was replaced by %s.", file1, file2);
      default:
        return null;
    }
  }
  
  
}
