package model;

import exceptions.ValidationException;

import java.util.Objects;

public class Flight {
    private final Airport from;
    private final Airport to;
    private final Time departureTime;
    private final int durationInMinutes;

    public Flight(Airport from, Airport to, Time departureTime, int durationInMinutes) throws ValidationException {
        if (from == null) {
            throw new ValidationException("Departure airport must not be null.");
        }
        if (to == null) {
            throw new ValidationException("Arrival airport must not be null.");
        }
        if (from.equals(to)) {
            throw new ValidationException("Departure and arrival airports must be different");
        }
        if (durationInMinutes <= 0) {
            throw new ValidationException("Flight duration must be a positive number of minutes");
        }
        this.from = from;
        this.to = to;
        this.departureTime = departureTime;
        this.durationInMinutes = durationInMinutes;
    }

    public Airport getFrom() {
        return from;
    }

    public Airport getTo() {
        return to;
    }

    public Time getDepartureTime() {
        return departureTime;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flight flight = (Flight) o;
        return durationInMinutes == flight.durationInMinutes
                && Objects.equals(from, flight.from)
                && Objects.equals(to, flight.to)
                && Objects.equals(departureTime, flight.departureTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, departureTime, durationInMinutes);
    }

    @Override
    public String toString() {
        return from.getCode() + " -> " + to.getCode()
                + " at " + departureTime + " (" + durationInMinutes + " min)";
    }
}