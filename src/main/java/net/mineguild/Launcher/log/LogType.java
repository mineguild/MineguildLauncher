package net.mineguild.Launcher.log;

public enum LogType {
  DEBUG, EXTENDED, MINIMAL;

  public boolean includes(LogType other) {
    return other.compareTo(this) >= 0;
  }

  public String toString() {
    return name().substring(0, 1) + name().substring(1).toLowerCase();
  }
}
