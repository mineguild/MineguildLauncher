package net.mineguild.Launcher.utils.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class ModpackExclusionStrategy implements ExclusionStrategy {
  private final Class<?> typeToSkip;

  public ModpackExclusionStrategy(Class<?> typeToSkip) {
    this.typeToSkip = typeToSkip;
  }

  public boolean shouldSkipClass(Class<?> clazz) {
    return (clazz == typeToSkip);
  }

  public boolean shouldSkipField(FieldAttributes f) {
    return f.getAnnotation(ModPackOnly.class) != null;
  }
}
