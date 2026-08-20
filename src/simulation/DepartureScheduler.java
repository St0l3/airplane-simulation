package simulation;

import model.Aircraft;
import model.Airport;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DepartureScheduler {

    private final Map<Airport, Integer> lastDepartureSlot = new HashMap<>();
    private final Map<Airport, LinkedList<Aircraft>> waitingQueues = new HashMap<>();

    public void tick(int simMinutes, List<Aircraft> aircrafts) {
        for (Aircraft a : aircrafts) {
            if (a.getState() == Aircraft.State.WAITING && !a.isQueued()
                    && a.getScheduledMinutes() <= simMinutes) {
                enqueue(a);
                a.setQueued(true);
            }
        }
        int slot = simMinutes / 10;
        for (Map.Entry<Airport, LinkedList<Aircraft>> e : waitingQueues.entrySet()) {
            LinkedList<Aircraft> queue = e.getValue();
            if (queue.isEmpty()) continue;
            Integer last = lastDepartureSlot.get(e.getKey());
            if (last == null || slot > last) {
                Aircraft a = queue.removeFirst();
                a.takeOff(slot * 10);
                lastDepartureSlot.put(e.getKey(), slot);
            }
        }
    }

    private void enqueue(Aircraft a) {
        Airport from = a.getFlight().getFrom();
        LinkedList<Aircraft> queue = waitingQueues.get(from);
        if (queue == null) {
            queue = new LinkedList<>();
            waitingQueues.put(from, queue);
        }
        int i = 0;
        while (i < queue.size() && queue.get(i).getScheduledMinutes() <= a.getScheduledMinutes()) {
            i++;
        }
        queue.add(i, a);
    }

    public void reset() {
        lastDepartureSlot.clear();
        waitingQueues.clear();
    }
}
