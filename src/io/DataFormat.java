package io;

import exceptions.FileException;
import exceptions.ValidationException;
import model.Airport;
import model.Data;
import model.Flight;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface DataFormat {

    public abstract void write(Data data, BufferedWriter out) throws IOException, FileException;
    public abstract Data read(BufferedReader in) throws IOException, FileException, ValidationException;
}
