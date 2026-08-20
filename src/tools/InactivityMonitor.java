package tools;

import listeners.InactivityListener;

public class InactivityMonitor implements Runnable {

    private static final long TIMEOUT_MILLIS = 60000;
    private static final long WARNING_MILLIS = 5000;

    private volatile long lastActionTime = System.currentTimeMillis();
    private volatile int pauseCount = 0;
    private volatile boolean running = true;

    private final InactivityListener listener;
    private final Thread thread;

    private boolean countdownActive = false;
    private int lastSecondsSent = -1;

    private int lastIdleSecondSent = -1;
    private boolean lastIdlePausedSent = false;

    public InactivityMonitor(InactivityListener listener) {
        this.listener = listener;
        this.thread = new Thread(this);
        this.thread.setDaemon(true);
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        running = false;
    }

    public void registerAction() {
        lastActionTime = System.currentTimeMillis();
    }

    public synchronized void pause() {
        pauseCount++;
    }

    public synchronized void resume() {
        if (pauseCount > 0) pauseCount--;
        registerAction();
    }

    @Override
    public void run() {
        while (running) {
            if (pauseCount > 0) {
                lastActionTime = System.currentTimeMillis();
                cancelCountdownIfActive();
                emitIdleCountdown((int) (TIMEOUT_MILLIS / 1000), true);
            } else {
                long remaining = TIMEOUT_MILLIS - (System.currentTimeMillis() - lastActionTime);
                if (remaining <= 0) {
                    emitIdleCountdown(0, false);
                    listener.timeout();
                    return;
                }
                int secondsLeft = (int) Math.ceil(remaining / 1000.0);
                emitIdleCountdown(secondsLeft, false);
                if (remaining <= WARNING_MILLIS) {
                    int seconds = secondsLeft;
                    if (!countdownActive) {
                        countdownActive = true;
                        lastSecondsSent = seconds;
                        listener.countdownStarted(seconds);
                    } else if (seconds != lastSecondsSent) {
                        lastSecondsSent = seconds;
                        listener.countdownTick(seconds);
                    }
                } else {
                    cancelCountdownIfActive();
                }
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private void cancelCountdownIfActive() {
        if (countdownActive) {
            countdownActive = false;
            lastSecondsSent = -1;
            listener.countdownCancelled();
        }
    }

    private void emitIdleCountdown(int secondsLeft, boolean paused) {
        if (secondsLeft != lastIdleSecondSent || paused != lastIdlePausedSent) {
            lastIdleSecondSent = secondsLeft;
            lastIdlePausedSent = paused;
            listener.idleCountdown(secondsLeft, paused);
        }
    }
}
