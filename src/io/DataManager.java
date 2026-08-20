package io;

import exceptions.FileException;
import exceptions.ValidationException;
import model.Data;

import java.io.*;
import java.text.DateFormat;

public class DataManager {
    private DataManager(){

    }

    private static void save(Data data, File f, DataFormat format) throws FileException {
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(f))){
            format.write(data,bw);
        }
        catch(IOException e){
            throw new FileException("Failed to save file: " + e.getMessage(), e);
        }
    }
    public static Data load(File f) throws FileException, ValidationException {
        DataFormat format = getSupportedDataFormat(f);
        return load(f,format);
    }
    private static Data load(File f, DataFormat format) throws FileException, ValidationException {
        try(BufferedReader br = new BufferedReader(new FileReader(f))){
            return format.read(br);
        }
        catch(IOException e) {
            throw new FileException("Failed to read file: " + e.getMessage(), e);
        }
    }
    public static void save(Data d, File f) throws FileException {
        DataFormat format = getSupportedDataFormat(f);
        save(d,f,format);
    }
    private static DataFormat getSupportedDataFormat(File f) throws FileException{
        String name = f.getName().toLowerCase();
        if(name.endsWith(".csv")) return new CsvFormat();
        else if(name.endsWith(".json")) return new JsonFormat();
        else{
            throw new FileException("'" + f.getName() + "' has an unsupported file type. ");
        }
    }
}
