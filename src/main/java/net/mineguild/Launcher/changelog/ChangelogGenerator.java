package net.mineguild.Launcher.changelog;

import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import net.mineguild.Launcher.changelog.ChangelogFileEntry.ChangelogAction;
import net.mineguild.Launcher.changelog.ChangelogModEntry.ChangelogModAction;
import net.mineguild.Launcher.utils.ChecksumUtil.ModPackEntry;
import net.mineguild.ModPack.Mod;
import net.mineguild.ModPack.ModPack;
import net.mineguild.ModPack.ModPackFile;

public class ChangelogGenerator {

  private ModPack fromPack;
  private ModPack toPack;

  public ChangelogGenerator(ModPack fromPack, ModPack toPack) {
    this.fromPack = fromPack;
    this.toPack = toPack;
  }

  public Changelog generate() {
    Changelog log = new Changelog();
    List<Mod> toMods = Lists.newArrayList(toPack.getMods().values());
    List<String> fileReplaces = Lists.newArrayList();
    List<String> versionChanges = Lists.newArrayList();
    for (Entry<String, ModPackFile> e : fromPack.getFiles().entrySet()) {
      if (toPack.getFilesByHash(e.getValue().getHash()).isEmpty()) {
        if (e.getValue() instanceof Mod) {
          Mod from = (Mod) e.getValue();
          boolean found = false;
          for (Mod m : toMods) {
            if (m.getInfo().getName().equals(from.getInfo().getName())) {
              log.addEntry(ChangelogEntryBuilder.create(from.getInfo(), m.getInfo(),
                  ChangelogModAction.VERSION_CHANGE));
              versionChanges.add(from.getInfo().getName());
              found = true;
              break;
            }
          }
          if (!found) {
            log.addEntry(ChangelogEntryBuilder.create(from.getInfo(), null,
                ChangelogModAction.REMOVE));
          }
        } else {
          if (toPack.getOther().containsKey(e.getKey())) {
            log.addEntry(ChangelogEntryBuilder.create(new ModPackEntry(e.getKey(), e.getValue()), new ModPackEntry(
                e.getKey(), toPack.getFileByPath(e.getKey())), ChangelogAction.REPLACE));
            fileReplaces.add(e.getKey());
          } else {
            log.addEntry(ChangelogEntryBuilder.create(new ModPackEntry(e.getKey(), e.getValue()),
                null, ChangelogAction.REMOVE));
          }
        }
      }
    }

    for (Entry<String, ModPackFile> e : toPack.getFiles().entrySet()) {
      if (fromPack.getFilesByHash(e.getValue().getHash()).isEmpty() && !fileReplaces.contains(e.getKey())) {
        if (e.getValue() instanceof Mod) {
          Mod from = (Mod) e.getValue();
          if(!versionChanges.contains(from.getInfo().getName())){
            log.addEntry(ChangelogEntryBuilder.create(from.getInfo(), null, ChangelogModAction.ADD));
          }
        } else {
          log.addEntry(ChangelogEntryBuilder.create(new ModPackEntry(e.getKey(), e.getValue()), null, ChangelogAction.ADD));
        }
      }
    }
    return log;
  }

}
