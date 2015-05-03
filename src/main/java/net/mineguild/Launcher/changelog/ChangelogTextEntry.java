package net.mineguild.Launcher.changelog;

import lombok.Getter;
import lombok.Setter;

import com.google.gson.annotations.Expose;

public class ChangelogTextEntry extends ChangelogEntry {


  public @Expose @Getter @Setter String text;


  public ChangelogTextEntry(String text) {
    this.text = text;
  }

}
