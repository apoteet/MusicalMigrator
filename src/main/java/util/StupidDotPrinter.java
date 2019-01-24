package util;

import java.io.PrintStream;

public class StupidDotPrinter implements Runnable {

    private PrintStream stream;
    private int interval;
    private boolean keepItUpBuddy;

    public StupidDotPrinter(PrintStream stream, int interval) {
        this.stream = stream;
        this.interval = interval;
        this.keepItUpBuddy = true;
    }

    @Override
    public void run() {
        while(keepItUpBuddy) {
            try {
                stream.print('.');
                Thread.sleep(interval);
            } catch (InterruptedException ie) {
            }
        }
    }

    public void stop() {
        keepItUpBuddy = false;
        stream.println();
    }
}
