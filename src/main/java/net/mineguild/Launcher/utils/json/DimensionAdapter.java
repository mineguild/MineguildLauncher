package net.mineguild.Launcher.utils.json;


import java.awt.Dimension;
import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class DimensionAdapter extends TypeAdapter<Dimension> {

  @Override
  public void write(JsonWriter out, Dimension value) throws IOException {
    if(value != null){
      out.beginArray();
      out.value(value.getWidth());
      out.value(value.getHeight());
      out.endArray();
    } else {
      out.beginArray();
      out.value(0);
      out.value(0);
      out.endArray();
    }
  }

  @Override
  public Dimension read(JsonReader in) throws IOException {
    if (in.hasNext()) {
      in.beginArray();
      int width = in.nextInt();
      int height = in.nextInt();
      in.endArray();
      return new Dimension(width, height);
    }
    return null;
  }

}
