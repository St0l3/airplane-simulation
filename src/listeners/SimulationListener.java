package listeners;

import model.Aircraft;
import simulation.SimulationState;

import java.util.List;

public interface SimulationListener {
    void simulationTick(int simMinutes, List<Aircraft> snapshot);
    void simulationStateChanged(SimulationState state);
    void simulationError(String message);
}
