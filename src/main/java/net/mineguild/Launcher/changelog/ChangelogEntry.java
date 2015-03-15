package net.mineguild.Launcher.changelog;


public abstract class ChangelogEntry {
  
  public abstract String getText();
  
  @Override
  public String toString(){
    return String.format("Entry: %s", getText());
  }
    
}
