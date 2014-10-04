package net.mineguild.Launcher.utils.json;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class MCVersionIndex {

  public Map<String, String> latest;
  public List<MCVersion> versions;

  public class MCVersion {
    public String id;
    public Date time;
    public Date releaseTime;
    public String type;
  }

}
