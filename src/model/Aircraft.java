package model;

public class Aircraft {

    public enum State { WAITING, FLYING, LANDED }

    private final Flight flight;
    private State state = State.WAITING;
    private boolean queued = false;
    private int actualDepartureMinutes = -1;
    private double curX;
    private double curY;

    public Aircraft(Flight flight) {
        this.flight = flight;
        this.curX = flight.getFrom().getX();
        this.curY = flight.getFrom().getY();
    }

    public Flight getFlight() {
        return flight;
    }

    public State getState() {
        return state;
    }

    public boolean isQueued() {
        return queued;
    }

    public void setQueued(boolean queued) {
        this.queued = queued;
    }

    public int getScheduledMinutes() {
        return flight.getDepartureTime().getTotalMinutes();
    }

    public int getActualDepartureMinutes() {
        return actualDepartureMinutes;
    }

    public double getCurX() {
        return curX;
    }

    public double getCurY() {
        return curY;
    }

    public void takeOff(int simMinutes) {
        this.actualDepartureMinutes = simMinutes;
        this.state = State.FLYING;
    }

    public void updatePosition(int simMinutes) {
        if (state != State.FLYING) return;
        double progress = (simMinutes - actualDepartureMinutes) / (double) flight.getDurationInMinutes();
        if (progress < 0) progress = 0;
        if (progress >= 1) {
            progress = 1;
            state = State.LANDED;
        }
        curX = flight.getFrom().getX() + progress * (flight.getTo().getX() - flight.getFrom().getX());
        curY = flight.getFrom().getY() + progress * (flight.getTo().getY() - flight.getFrom().getY());
    }

    public Aircraft copy() {
        Aircraft a = new Aircraft(flight);
        a.state = state;
        a.queued = queued;
        a.actualDepartureMinutes = actualDepartureMinutes;
        a.curX = curX;
        a.curY = curY;
        return a;
    }

    @Override
    public String toString() {
        return flight.toString() + " [" + state + "]";
    }
}
