package net.mineguild.Launcher.logging;

public class Logger {
  
  private static Logger instance;
  
  public static Logger getLogger(){
    if(instance == null){
      instance = new Logger();
    }
    return instance;
  }
  
  

}
