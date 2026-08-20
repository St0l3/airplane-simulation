package listeners;

public interface InactivityListener {
    void countdownStarted(int secondsLeft);
    void countdownTick(int secondsLeft);
    void countdownCancelled();
    void timeout();

    // Fired (at most once per changed second) with the time left before auto-close.
    // paused == true means the idle timer is currently suspended (no auto-close pending).
    void idleCountdown(int secondsLeft, boolean paused);
}
