package cn.edu.thssdb.exception;


public class DatabaseNotExistException extends RuntimeException {
    private String key;

    public DatabaseNotExistException()
    {
        super();
        this.key = null;
    }

    public DatabaseNotExistException(String key)
    {
        super();
        this.key = key;
    }

    @Override
    public String getMessage() {
        if (key == null)
            return "Exception: database doesn't exist!";
        else
            return "Exception: database \"" + this.key + "\" doesn't exist!";
    }
}
