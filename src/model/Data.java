package model;

import exceptions.ValidationException;
import listeners.DataListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Data {
    private final List<Airport> airports = new ArrayList<>();
    private final List<Flight> flights = new ArrayList<>();

    private final List<DataListener> listeners = new ArrayList<>();
    private Airport selectedAirport;

    public synchronized List<Airport> getAirports() {
        return airports;
    }

    public synchronized List<Flight> getFlights() {
        return flights;
    }
    public void addModelListener(DataListener l) {
        if (l != null && !listeners.contains(l)) listeners.add(l);
    }

    public void removeModelListener(DataListener l) {
        listeners.remove(l);
    }
    protected void fireModelChanged() {
        for (DataListener l : new ArrayList<DataListener>(listeners)) {
            l.dataChanged();
        }
    }
    public synchronized  Airport findAirportByCode(String code){
        if (code == null) return null;
        for(Airport a : airports){
            if (a.getCode().equals(code)) return a;
        }
        return null;
    }
    public void addFlight(Flight f){
        synchronized (this){
            flights.add(f);
        }
        fireModelChanged();
    }
    public void addAirport(Airport a) throws ValidationException {
        synchronized (this) {
            Airport existing = findAirportByCode(a.getCode());
            if (existing != null) {
                throw new ValidationException(
                        "An airport with code '" + a.getCode() + "' already exists ("
                                + existing.getName() + "). Codes must be unique, "
                                + "choose a different code.");
            }
            airports.add(a);
        }
        fireModelChanged();
    }
    public void removeFlight(Flight f){
        synchronized (this){
            flights.remove(f);
        }
        fireModelChanged();
    }
    public void removeAirport(Airport a){
        synchronized (this){
            airports.remove(a);
            for (Iterator<Flight> it = flights.iterator(); it.hasNext(); ) {
                Flight f = it.next();
                if (f.getFrom().equals(a) || f.getTo().equals(a)) it.remove();
            }
            if (a.equals(selectedAirport)) selectedAirport = null;
        }
        fireModelChanged();
    }
    public void clear(){
        synchronized (this){
            airports.clear();
            flights.clear();
            selectedAirport = null;
        }
        fireModelChanged();
    }
    public synchronized Airport getSelectedAirport(){
        return selectedAirport;
    }
    public void setSelectedAirport(Airport a){
        synchronized (this){
            selectedAirport = a;
        }
        fireModelChanged();
    }
    public void setAirportVisible(Airport a, boolean visible){
        a.setVisible(visible);
        fireModelChanged();
    }
    public void replaceAll(Data other){
        synchronized (this){
            airports.clear();
            flights.clear();
            airports.addAll(other.getAirports());
            flights.addAll(other.getFlights());
            selectedAirport = null;
        }
        fireModelChanged();
    }

    @Override
    public String toString() {
        return airports +"\nFlights:\n" + flights;
    }
}
