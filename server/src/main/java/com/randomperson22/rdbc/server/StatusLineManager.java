package com.randomperson22.rdbc.server;

import java.util.concurrent.atomic.AtomicReference;

public class StatusLineManager {

    private static class Line {
        volatile String text;
        volatile boolean active = true;
    }

    private final AtomicReference<Line> current = new AtomicReference<>(new Line());

    public StatusLineManager() {
        Thread printer = new Thread(() -> {
            String lastPrinted = "";
            while (true) {
                Line line = current.get();

                if (line != null && line.active) {
                    String output = "\r" + line.text;

                    // only rewrite if changed (prevents flicker)
                    if (!output.equals(lastPrinted)) {
                        System.out.print(output + "   ");
                        lastPrinted = output;
                    }
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {}
            }
        });

        printer.setDaemon(true);
        printer.start();
    }

    public synchronized void update(String text) {
        Line line = current.get();
        if (line != null) {
            line.text = text;
        }
    }

    public synchronized void newLine(String text) {
        // freeze old line
        Line old = current.get();
        if (old != null) old.active = false;

        // create new active line
        Line fresh = new Line();
        fresh.text = text;
        current.set(fresh);
    }
}