package io;

import exceptions.FileException;
import exceptions.ValidationException;
import model.Airport;
import model.Data;
import model.Flight;
import tools.Validator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class CsvFormat implements DataFormat{
    private static final String AIRPORT_MARKER = "# AIRPORTS";
    private static final String FLIGHT_MARKER  = "# FLIGHTS";
    private static final String AIRPORT_HEADER = "CODE,NAME,X,Y";
    private static final String FLIGHT_HEADER  = "FROM,TO,DEPARTURE,DURATION";
    @Override
    public void write(Data data, BufferedWriter out) throws IOException, FileException {
        out.write(AIRPORT_MARKER + "\n");
        out.write(AIRPORT_HEADER + "\n");
        List<Airport> airports = data.getAirports();
        List<Flight> flights = data.getFlights();
        for(Airport a : airports){
            if(a.getName().contains(","))
                throw new FileException("The name of the airport cannot contain ','");
            out.write(a.getCode()+","+a.getName()+","+a.getX()+","+a.getY()+"\n");
        }
        out.write(FLIGHT_MARKER + "\n");
        out.write(FLIGHT_HEADER + "\n");
        for(Flight f : flights){
            out.write(f.getFrom().getCode()+","+f.getTo().getCode()+","+f.getDepartureTime()+","+f.getDurationInMinutes()+"\n");
        }
    }
    private static final int BEFORE_AIRPORTS = 0;
    private static final int IN_AIRPORTS     = 1;
    private static final int IN_FLIGHTS      = 2;
    @Override
    public Data read(BufferedReader in) throws IOException, FileException, ValidationException {
        Data d = new Data();
        int section = BEFORE_AIRPORTS;
        boolean airportHeaderSeen = false;
        boolean flightHeaderSeen = false;

        BufferedReader reader = new BufferedReader(in);
        String rawLine;
        int lineNo = 0;

        while ((rawLine = reader.readLine()) != null) {
            lineNo++;
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (section == BEFORE_AIRPORTS) {
                if (!line.equalsIgnoreCase(AIRPORT_MARKER)) {
                    throw new FileException("Expected the section marker '" + AIRPORT_MARKER
                            + "' but found '" + line + "'. The file must start with the airports section.");
                }
                section = IN_AIRPORTS;

            } else if (section == IN_AIRPORTS) {
                if (!airportHeaderSeen) {
                    if (!line.equalsIgnoreCase(AIRPORT_HEADER)) {
                        throw new FileException("Expected the column header '"
                                + AIRPORT_HEADER + "' but found '" + line);
                    }
                    airportHeaderSeen = true;
                } else if (line.equalsIgnoreCase(FLIGHT_MARKER)) {
                    section = IN_FLIGHTS;
                } else {
                    String[] p = splitRow(line, AIRPORT_HEADER);
                    d.addAirport(Validator.validateAirport(p[0],p[1],p[2],p[3],d));
                }

            } else {
                if (!flightHeaderSeen) {
                    if (!line.equalsIgnoreCase(FLIGHT_HEADER)) {
                        throw new FileException("Expected the column header '"
                                + FLIGHT_HEADER + "' but found '" + line);
                    }
                    flightHeaderSeen = true;
                } else {
                    String[] p = splitRow(line, FLIGHT_HEADER);
                        d.addFlight(Validator.validateFlight(p[0], p[1], p[2], p[3],d));
                }
            }
        }
        if (section == BEFORE_AIRPORTS) {
            throw new FileException("File is empty. Expected the section marker '"
                    + AIRPORT_MARKER + "' followed by the airport rows.");
        }
        if (section == IN_AIRPORTS) {
            throw new FileException("File ends before the '" + FLIGHT_MARKER
                    + "' section.");
        }
        if (!flightHeaderSeen) {
            throw new FileException("File ends before the flights column header. ");
        }
        return  d;
    }
    private static String[] splitRow(String line, String header)
            throws FileException {
        String[] parts = line.split(",", -1);
        if (parts.length != 4) {
            throw new FileException("Row must have exactly 4 columns ("
                    + header + ") but has " + parts.length);
        }
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        return parts;
    }
}
