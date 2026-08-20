package io;
import exceptions.FileException;
import exceptions.ValidationException;
import io.DataFormat;
import model.Airport;
import model.Data;
import model.Flight;
import tools.Validator;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonFormat implements DataFormat {

    @Override
    public void write(Data data, BufferedWriter out) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"airports\": [");

        List<Airport> airports = data.getAirports();
        for (int i = 0; i < airports.size(); i++) {
            Airport a = airports.get(i);
            sb.append(i == 0 ? "\n" : ",\n");
            sb.append("    { \"code\": ").append(quote(a.getCode()))
                    .append(", \"name\": ").append(quote(a.getName()))
                    .append(", \"x\": ").append(a.getX())
                    .append(", \"y\": ").append(a.getY())
                    .append(" }");
        }
        sb.append(airports.isEmpty() ? "]" : "\n  ]").append(",\n  \"flights\": [");
        List<Flight> flights = data.getFlights();
        for (int i = 0; i < flights.size(); i++) {
            Flight f = flights.get(i);
            sb.append(i == 0 ? "\n" : ",\n");
            sb.append("    { \"from\": ").append(quote(f.getFrom().getCode()))
                    .append(", \"to\": ").append(quote(f.getTo().getCode()))
                    .append(", \"departure\": ").append(quote(f.getDepartureTime().toString()))
                    .append(", \"duration\": ").append(f.getDurationInMinutes())
                    .append(" }");
        }
        sb.append(flights.isEmpty() ? "]" : "\n  ]").append("\n}\n");
        out.write(sb.toString());
    }
    private String src;
    private int pos;
    @Override
    public Data read(BufferedReader in) throws IOException, FileException, ValidationException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) sb.append(line).append('\n');
        src = sb.toString();
        pos = 0;

        Data d = new Data();

        expect('{');
        expectKey("airports");
        expect('[');
        if (peek() != ']') {
            do {
                expect('{');
                String code = null, name = null, x = null, y = null;
                do {
                    String key = readString();
                    expect(':');
                    if (key.equals("code")) code = readString();
                    else if (key.equals("name")) name = readString();
                    else if (key.equals("x")) x = readNumber();
                    else if (key.equals("y")) y = readNumber();
                    else throw new FileException("Unknown field '" + key + "' in airports.");
                } while (accept(','));
                expect('}');
                if (code == null || name == null || x == null || y == null)
                    throw new FileException("An airport is missing one of: code, name, x, y.");
                d.addAirport(Validator.validateAirport(code, name, x, y, d));
            } while (accept(','));
        }
        expect(']');
        expect(',');
        expectKey("flights");
        expect('[');
        if (peek() != ']') {
            do {
                expect('{');
                String from = null, to = null, dep = null, dur = null;
                do {
                    String key = readString();
                    expect(':');
                    if (key.equals("from")) from = readString();
                    else if (key.equals("to")) to = readString();
                    else if (key.equals("departure")) dep = readString();
                    else if (key.equals("duration")) dur = readNumber();
                    else throw new FileException("Unknown field '" + key + "' in flights.");
                } while (accept(','));
                expect('}');
                if (from == null || to == null || dep == null || dur == null)
                    throw new FileException("A flight is missing one of: from, to, departure, duration.");
                d.addFlight(Validator.validateFlight(from, to, dep, dur, d));
            } while (accept(','));
        }
        expect(']');
        expect('}');

        return d;
    }
    private void skipWs() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
    }

    private char peek() throws FileException {
        skipWs();
        if (pos >= src.length()) throw new FileException("Unexpected end of JSON file.");
        return src.charAt(pos);
    }

    private void expect(char c) throws FileException {
        if (peek() != c)
            throw new FileException("Expected '" + c + "' at position " + pos
                    + " but found '" + src.charAt(pos) + "'.");
        pos++;
    }

    private boolean accept(char c) throws FileException {
        skipWs();
        if (pos < src.length() && src.charAt(pos) == c) { pos++; return true; }
        return false;
    }

    private void expectKey(String name) throws FileException {
        String key = readString();
        if (!key.equals(name))
            throw new FileException("Expected the field '" + name + "' but found '" + key + "'.");
        expect(':');
    }

    private String readString() throws FileException {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (pos < src.length() && src.charAt(pos) != '"') {
            char c = src.charAt(pos++);
            if (c == '\\') {
                if (pos >= src.length()) throw new FileException("Unterminated escape in string.");
                char e = src.charAt(pos++);
                if (e == 'n') sb.append('\n');
                else if (e == 't') sb.append('\t');
                else sb.append(e);
            } else {
                sb.append(c);
            }
        }
        if (pos >= src.length()) throw new FileException("Unterminated string at end of file.");
        pos++;
        return sb.toString();
    }

    private String readNumber() throws FileException {
        skipWs();
        int start = pos;
        if (pos < src.length() && (src.charAt(pos) == '-' || src.charAt(pos) == '+')) pos++;
        while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.')) pos++;
        if (start == pos) throw new FileException("Expected a number at position " + pos + ".");
        return src.substring(start, pos);
    }

    private static String quote(String s) {
        return '"' + s.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }
}