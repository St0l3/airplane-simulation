package model;

import exceptions.ValidationException;

import java.util.Objects;

public class Airport {

    private final String name;
    private final String code;
    // Moze i treba short da stoji
    private final int x;

    private final int y;
    private boolean visible = true;


    public Airport(String name, String code, int x, int y) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Airport name must not be empty.");
        }
        if (code == null || !code.matches("[A-Z]{3}")) {
            throw new ValidationException("Airport code '" + code + "' must be exactly 3 uppercase letters (e.g. BEG).");
        }
        if (x < -180 || x > 180) {
            throw new ValidationException("X[" + x + "] is out of range. It must be between -180 and 180.");
        }
        if (y < -90 || y > 90) {
            throw new ValidationException("Y[" + y + "] is out of range. It must be between -90 and 90.");
        }
        this.name = name.trim();
        this.code = code;
        this.x = x;
        this.y = y;
    }
    public Boolean isVisible(){
        return visible;
    }
    public void setVisible(Boolean visible){
        this.visible = visible;
    }
    public String getCode() {
        return code;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Airport)) return false;
        return code.equals(((Airport) o).code);
    }

    @Override
    public String toString() {
        return "[" + code +"] - " + name + "(" + x+", " + y+")";
    }
}
