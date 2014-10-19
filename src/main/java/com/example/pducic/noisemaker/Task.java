package com.example.pducic.noisemaker;

import android.os.Process;
import android.util.Log;

/**
 * Created by pducic on 24.09.14.
 */
//TODO use android intent service
public abstract class Task implements Runnable {

    private boolean running = false;
    private volatile Thread thread = null;

    protected abstract void process();

    void start(){
        synchronized (this) {
            if (running)
                return;

            thread = new Thread(this);
            Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            running = true;
            thread.start();
        }
    }

    void stop() {
        thread = null;
    }

    boolean isRunning() {
        return thread != null;
    }

    @Override
    public void run() {
        try {
            while (thread == Thread.currentThread()) {
                process();
            }
        } catch (Exception e) {
            Log.e("Task", "Interrupted", e);
        }
        synchronized (this) {
            running = false;
        }
    }
}