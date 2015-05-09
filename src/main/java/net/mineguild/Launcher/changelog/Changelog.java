package net.mineguild.Launcher.changelog;

import java.util.Collections;
import java.util.List;

import net.mineguild.Launcher.log.Logger;

import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;

public class Changelog {

  private @Expose List<ChangelogEntry> entries;

  public Changelog() {
    entries = Lists.newArrayList();
  }

  public void addEntry(ChangelogEntry entry) {
    if (!entries.contains(entry)) {
      entries.add(entry);
    } else {
      Logger.logError("Tried to add entry twice!");
    }
  }

  public List<ChangelogEntry> getEntries() {
    return Collections.unmodifiableList(entries);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (ChangelogEntry e : entries) {
      builder.append(e.getText());
      builder.append("\n");
    }
    return builder.toString();
  }


}
