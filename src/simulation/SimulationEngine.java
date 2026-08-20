package simulation;

import exceptions.SimulationException;
import listeners.SimulationListener;
import model.Aircraft;
import model.Data;
import model.Flight;

import java.util.ArrayList;
import java.util.List;

public class SimulationEngine implements Runnable {

    private static final long TICK_MILLIS = 200;
    private static final int MINUTES_PER_TICK = 2;

    private final Data data;
    private final DepartureScheduler scheduler = new DepartureScheduler();
    private final List<Aircraft> aircrafts = new ArrayList<>();
    private final List<SimulationListener> listeners = new ArrayList<>();

    private volatile SimulationState state = SimulationState.STOPPED;
    private Thread worker;
    private int simMinutes = 0;

    public SimulationEngine(Data data) {
        this.data = data;
    }

    public void addSimulationListener(SimulationListener l) {
        if (l != null && !listeners.contains(l)) listeners.add(l);
    }

    public SimulationState getState() {
        return state;
    }

    public int getSimMinutes() {
        return simMinutes;
    }

    public void start() throws SimulationException {
        if (state != SimulationState.STOPPED) return;
        List<Flight> flights = new ArrayList<>(data.getFlights());
        if (flights.isEmpty()) {
            throw new SimulationException("There are no flights to simulate. Add flights in the Data tab or load a file first.");
        }
        aircrafts.clear();
        scheduler.reset();
        simMinutes = 0;
        for (Flight f : flights) {
            aircrafts.add(new Aircraft(f));
        }
        state = SimulationState.RUNNING;
        fireStateChanged();
        fireTick();
        worker = new Thread(this);
        worker.setDaemon(true);
        worker.start();
    }

    public void pause() {
        if (state == SimulationState.RUNNING) {
            state = SimulationState.PAUSED;
            fireStateChanged();
        }
    }

    public void resume() {
        if (state == SimulationState.PAUSED) {
            state = SimulationState.RUNNING;
            fireStateChanged();
        }
    }

    public void reset() {
        state = SimulationState.STOPPED;
        if (worker != null) {
            try {
                worker.join(1000);
            } catch (InterruptedException ignored) {
            }
            worker = null;
        }
        aircrafts.clear();
        scheduler.reset();
        simMinutes = 0;
        fireStateChanged();
        fireTick();
    }

    @Override
    public void run() {
        long last = System.nanoTime();
        double accumulator = 0;
        while (state != SimulationState.STOPPED) {
            long now = System.nanoTime();
            if (state == SimulationState.RUNNING) {
                accumulator += (now - last) / 1_000_000.0;
                while (accumulator >= TICK_MILLIS) {
                    accumulator -= TICK_MILLIS;
                    tick();
                }
            } else {
                accumulator = 0;
            }
            last = now;
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private void tick() {
        try {
            simMinutes += MINUTES_PER_TICK;
            scheduler.tick(simMinutes, aircrafts);
            for (Aircraft a : aircrafts) {
                a.updatePosition(simMinutes);
            }
            fireTick();
        } catch (Exception e) {
            SimulationException se = new SimulationException(
                    "Simulation paused because of an unexpected error: " + e.getMessage() + ". Press Resume to continue.", e);
            state = SimulationState.PAUSED;
            fireStateChanged();
            for (SimulationListener l : listeners) {
                l.simulationError(se.getMessage());
            }
        }
    }

    private void fireTick() {
        List<Aircraft> snapshot = new ArrayList<>();
        for (Aircraft a : aircrafts) {
            snapshot.add(a.copy());
        }
        for (SimulationListener l : listeners) {
            l.simulationTick(simMinutes, snapshot);
        }
    }

    private void fireStateChanged() {
        for (SimulationListener l : listeners) {
            l.simulationStateChanged(state);
        }
    }
}
