package com.garbageTier.util;

import java.io.PrintStream;

public class ProgressTracker implements Runnable {

    private PrintStream stream;
    private int progress;
    private int lastPrintedNumber;
    private int maxVal;
    private boolean keepItUpBuddy;

    public ProgressTracker(PrintStream stream, int maxVal) {
        this.stream = stream;
        this.progress = 0;
        this.lastPrintedNumber = -1;
        this.maxVal = maxVal;
        this.keepItUpBuddy = true;
    }

    @Override
    public void run() {
        do {
            try {
                if (this.progress != this.lastPrintedNumber) {
                    stream.println("Progress: " + this.progress + " / " + this.maxVal);
                    this.lastPrintedNumber = this.progress;
                }
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
            }
        } while (keepItUpBuddy);
        if (this.progress != this.lastPrintedNumber) {
            stream.println("Progress: " + this.progress + " / " + this.maxVal);
            this.lastPrintedNumber = this.progress;
        }
    }

    public void increment() {
        progress++;
    }

    public void stop() {
        keepItUpBuddy = false;
    }
}
