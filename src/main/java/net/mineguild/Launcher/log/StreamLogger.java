package net.mineguild.Launcher.log;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import net.mineguild.Launcher.MineguildLauncher;

public class StreamLogger extends Thread {
    private final InputStream is;
    private final LogEntry logInfo;
    private String[] ignore;

    @Getter private static StreamLogger instance;

    private StreamLogger(InputStream from, LogEntry logInfo) {
        instance = this;
        this.is = from;
        this.logInfo = logInfo;
    }

    @Override public void run() {
        byte buffer[] = new byte[4096];
        String logBuffer = "";
        int newLineIndex;
        int nullIndex;
        try {
            while (is.read(buffer) > 0) {
                logBuffer += new String(buffer).replace("\r\n", "\n");
                nullIndex = logBuffer.indexOf(0);
                if (nullIndex != -1) {
                    logBuffer = logBuffer.substring(0, nullIndex);
                }
                while ((newLineIndex = logBuffer.indexOf("\n")) != -1) {
                    if (ignore != null) {
                        boolean skip = false;
                        for (String s : ignore) {
                            if (logBuffer.substring(0, newLineIndex).contains(s)) {
                                skip = true;
                            }
                        }
                        if (!skip) {
                            if (logBuffer.substring(0, newLineIndex)
                                .contains("#@!@# Game crashed! Crash report saved to: #@!@#")) {
                                if (MineguildLauncher.getLFrame() != null) {
                                    MineguildLauncher.getLFrame().setCrashed(true);
                                }
                            }
                            Logger.log(new LogEntry().copyInformation(logInfo)
                                .message(logBuffer.substring(0, newLineIndex)));
                        }
                    } else {
                        Logger.log(new LogEntry().copyInformation(logInfo)
                            .message(logBuffer.substring(0, newLineIndex)));
                    }
                    logBuffer = logBuffer.substring(newLineIndex + 1);
                }
                Arrays.fill(buffer, (byte) 0);
            }
        } catch (IOException e) {
            Logger.logError(
                "Error while reading log messages from external source(minecraft process)", e);
        }
    }

    /**
     * Creates StreamLogger object
     *
     * @param from    InputStream to read incoming log
     * @param logInfo default LogEntry configuration
     */
    public static void prepare(InputStream from, LogEntry logInfo) {
        logInfo.source(LogSource.EXTERNAL);
        instance = new StreamLogger(from, logInfo);
    }

    /**
     * Starts external process logger
     */
    public static void doStart() {
        instance.start();
    }

    /**
     * Sets StreamLogger to stop logging certain messages. Uses String.contains() when comparing given
     * strings to single external log line
     *
     * @param ignore Array containing Strings which are used to ignore lines from LogListeteners
     */
    public static void setIgnore(String[] ignore) {
        instance.ignore = ignore;
    }
}
