package tools;

import exceptions.ValidationException;
import model.Airport;
import model.Data;
import model.Flight;
import model.Time;

public final class Validator {
    public static Airport validateAirport(String code, String name, String xText, String yText, Data data) throws ValidationException {
        String c = code==null ? "" : validateCode(code.trim());
        String n = name == null ? "" : name.trim();
        if (c.isEmpty()) {
            throw new ValidationException(
                    "Airport code is required. Enter exactly 3 uppercase letters, e.g. BEG.");
        }
        if (n.isEmpty()) {
            throw new ValidationException(
                    "Airport name is required. Enter the full name, e.g. Belgrade Nikola Tesla.");
        }
        if(data != null) {
            Airport a = data.findAirportByCode(code);
            if(a!=null){
                throw new ValidationException("An airport with code '" + c + "' already exists (" + a.getName()
                        + "). Codes must be unique, choose a different code.");
            }
        }
        int x = validateInteger(xText);
        int y = validateInteger(yText);
        x = validateCoordinateBounds("x",x,-180,180);
        y = validateCoordinateBounds("y",y,-90,90);
        return new Airport(n,c,x,y);
    }
    public static Flight validateFlight(String fromCode, String toCode, String timeText, String durationText, Data d) throws ValidationException{
        if(d == null) {
            throw new ValidationException("Data cannot be null");
        }
        String f = fromCode == null ? "" : validateCode(fromCode.trim());
        String t = toCode == null ? "" : validateCode(toCode.trim());
        if (f.isEmpty()) {
            throw new ValidationException(
                    "Departure airport is required.");
        }
        if (t.isEmpty()) {
            throw new ValidationException(
                    "Destination airport is required.");
        }
        Airport from = d.findAirportByCode(f);
        if(from==null){
            throw new ValidationException("Unknown airport code '" +f+"' for departure");
        }
        Airport to = d.findAirportByCode(t);
        if(to==null){
            throw new ValidationException("Unknown airport code '" +t+"' for departure");
        }
        if(from.equals(to)){
            throw new ValidationException(
                    "A flight cannot depart from and arrive at the same airport ('" + f
                            + "')");
        }
        Time time = new Time(timeText);
        int duration = validateInteger(durationText);
        if(duration <= 0){
            throw new ValidationException("Flight duration must be a positive number of minutes, got '" + durationText + "'");
        }
        return new Flight(from,to,time,duration);
    }
    private static int validateCoordinateBounds(String coordName, int coordVal, int leftBound, int rightBound) throws ValidationException{
        if(coordVal<leftBound || coordVal>rightBound){
            throw new ValidationException("Coordinate '" +coordName+"' is not in the range: ["+leftBound+", "+rightBound+"]");
        }
        return coordVal;
    }
    private static int validateInteger(String number) throws ValidationException{
        int n;
        try{
            n = Integer.parseInt(number);
        }
        catch (NumberFormatException e)
        {
            throw new ValidationException("'"+number+"' is not a number", e);
        }
        return n;
    }
    private static String validateCode(String code) throws ValidationException{
       if(!code.matches("[A-Z]{3}"))
           throw new ValidationException(
                   "Airport code '" + code+ "' must be exactly 3 uppercase letters (e.g. BEG).");
       return code;
    }
}
