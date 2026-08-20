package exceptions;

public class FileException extends AppException {
    public FileException(String message){
        super(message);
    }

    public FileException(String message, Throwable cause){
        super(message, cause);
    }
}
