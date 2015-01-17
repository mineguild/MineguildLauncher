package net.mineguild.Launcher.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class LogWriter implements ILogListener {
    private final BufferedWriter logWriter;
    private final LogSource source;

    public LogWriter(File logFile, LogSource source) throws IOException {
        this.source = source;
        this.logWriter =
            new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile), "UTF-8"));
        this.logWriter
            .write(logFile + ": MineguildLauncher log." + System.getProperty("line.separator"));
        this.logWriter.flush();
    }

    @Override public void onLogEvent(LogEntry entry) {
        if (source == LogSource.ALL || entry.source == source) {
            try {
                logWriter
                    .write(entry.toString(LogType.EXTENDED) + System.getProperty("line.separator"));
                logWriter.flush();
            } catch (IOException e) {
                // We probably do not want to trigger new errors
                // How can we notify user? Is notify needed?
                // Logger.logError("Error while writing logs", e);
            }
        }
    }
}
