package model;

import exceptions.ValidationException;

import java.util.Objects;

public class Time implements Comparable<Time>
{
    // Gleda se u odnosu na ponoc;
    private int timeInMinutes;
    private int days;

    public Time(String text) throws ValidationException {
        parse(text);
    }
    public Time(int timeInMinutes){
        this.days = Math.floorDiv(timeInMinutes, 1440);
        this.timeInMinutes = Math.floorMod(timeInMinutes, 1440);
    }
    public Time(int hours, int minutes){
        this(hours*60 + minutes);
    }

    public int getDays() {
        return days;
    }

    public Time addMinutes(int minutes){
        Time t = new Time(this.timeInMinutes + minutes);
        t.days += this.days;
        return t;
    }
    public Time addTime(Time t){
        Time r = new Time(this.timeInMinutes + t.timeInMinutes);
        r.days += this.days + t.days;
        return r;
    }
    public Time addTime(int h, int m){
        return addMinutes(h*60 + m);
    }
    public int getTimeInMinutes() {
        return timeInMinutes;
    }
    public int toHours(){
        return timeInMinutes/60;
    }
    public int toMinutes(){
        return timeInMinutes;
    }
    public int getTotalMinutes(){
        return days*1440 + timeInMinutes;
    }
    public int getMinutes(){
        return timeInMinutes%60;
    }
    private void parse(String text) throws ValidationException {
        if(text == null){
            throw new ValidationException("Time can't be empty string");
        }
        String s = text.trim();
        if(s.length()!=5 || s.charAt(2)!=':'){
            throw new ValidationException("Time must be in HH:MM format ("+text+")");
        }
        String hh = s.substring(0,2);
        String mm = s.substring(3,5);
        try{
            int h=   Integer.parseInt(hh);
            int m=  Integer.parseInt(mm);
            if (h > 23) {
                throw new ValidationException("Hours must be a value between 0-23");
            }
            if (m > 59) {
                throw new ValidationException("Minutes must be a value between 0-59");
            }
            this.timeInMinutes = (h*60+m);
            this.days = 0;
        }
        catch (NumberFormatException e){
            throw new ValidationException("Time must contain only digits in HH:MM format", e);
        }
    }
    public String format(){

        int h = toHours();
        int m = getMinutes();
        return ((h<10) ? "0" : "") + h + ":" +((m<10) ? "0" : "") + m;

    }
    @Override
    public int compareTo(Time o) {
        return Integer.compare(this.getTotalMinutes(), o.getTotalMinutes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Time time = (Time) o;
        return timeInMinutes == time.timeInMinutes && days == time.days;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeInMinutes, days);
    }

    @Override
    public String toString() {
        return format();
    }
}