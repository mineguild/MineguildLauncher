package net.mineguild.Launcher.minecraft;

import net.mineguild.Launcher.log.Logger;

public class ProcessMonitor implements Runnable {

    private final Process process;
    private final Runnable onComplete;

    private ProcessMonitor(Process proc, Runnable onComplete) {
        this.process = proc;
        this.onComplete = onComplete;
    }

    public void run() {
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Logger.logError("ProcessMonitor was interrupted", e);
        }
        onComplete.run();
    }

    public static ProcessMonitor create(Process proc, Runnable onComplete) {
        ProcessMonitor processMonitor = new ProcessMonitor(proc, onComplete);
        Thread monitorThread = new Thread(processMonitor);
        monitorThread.start();
        return processMonitor;
    }

    public void stop() {
        if (process != null)
            process.destroy();
    }

}
