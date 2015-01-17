package net.mineguild.Launcher.log;

import java.io.PrintStream;

import lombok.Setter;

public class StdOutLogger implements ILogListener {
    // save real System.out and System.err
    // otherwise we'll got nasty loop
    private final static PrintStream realStderr = System.err;
    private final static PrintStream realStdout = System.out;

    // DEBUG, EXTENTED, MINIMAL
    // how to write. Debug is only needed if we want to
    // see source of the log message. Hardcoded to EXTENDED
    private LogType logType = LogType.EXTENDED;

    // ALL, LAUNCHER, EXTERNAL
    // which sources to write
    @Setter private LogSource logSource = LogSource.LAUNCHER;

    // INFO, WARN, ERROR, UNKNOWN
    // which severities to write. Not used in Console or LogWriter
    @Setter private LogLevel logLevel = LogLevel.UNKNOWN;

    public StdOutLogger() {
    }

    public StdOutLogger(LogSource logSource) {
        this.logSource = logSource;
    }

    @Override public void onLogEvent(LogEntry entry) {
        if (entry.source != logSource)
            return;

        if (entry.level == LogLevel.ERROR) {
            realStderr.println(entry.toString(logType));
        } else {
            realStdout.println(entry.toString(logType));
        }
    }
}
