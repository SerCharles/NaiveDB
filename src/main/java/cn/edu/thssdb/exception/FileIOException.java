package cn.edu.thssdb.exception;

public class FileIOException extends RuntimeException{
    private String filename;

    public FileIOException(String filename)
    {
        super();
        this.filename = filename;
    }

    @Override
    public String getMessage()
    {
        return "Exception: fail to read/write file: " + filename + "!";
    }
}
