package net.mineguild.Launcher.log;

public enum LogLevel {
  DEBUG, INFO, WARN, ERROR, UNKNOWN;

  public boolean includes(LogLevel other) {
    return other.compareTo(this) >= 0;
  }
}
