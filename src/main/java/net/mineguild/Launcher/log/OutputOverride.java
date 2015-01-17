package net.mineguild.Launcher.log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class OutputOverride extends PrintStream {
    final LogLevel level;

    public OutputOverride(OutputStream str, LogLevel type) {
        super(str);
        this.level = type;
    }

    @Override public void write(byte[] b) throws IOException {
        // super.write(b);
        String text = new String(b).trim();
        if (!text.equals("") && !text.equals("\n")) {
            Logger.log("From Console: " + text, level, null);
        }
    }

    @Override public void write(byte[] buf, int off, int len) {
        // super.write(buf, off, len);
        String text = new String(buf, off, len).trim();
        if (!text.equals("") && !text.equals("\n")) {
            Logger.log("From Console: " + text, level, null);
        }
    }

    @Override public void write(int b) {
        throw new UnsupportedOperationException("Write(int) is not supported by OutputOverride.");
    }
}
