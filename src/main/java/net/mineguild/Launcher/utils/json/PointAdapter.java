package net.mineguild.Launcher.utils.json;

import java.awt.Point;
import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class PointAdapter extends TypeAdapter<Point> {

  @Override
  public void write(JsonWriter out, Point value) throws IOException {
    if(value != null){
      out.beginArray();
      out.value(value.getX());
      out.value(value.getY());
      out.endArray();
    } else {
      out.beginArray();
      out.value(0.0);
      out.value(0.0);
      out.endArray();
    }
  }

  @Override
  public Point read(JsonReader in) throws IOException {
    in.beginArray();
    double x = in.nextDouble();
    double y = in.nextDouble();
    in.endArray();
    return new Point((int) x, (int) y);
  }

}
