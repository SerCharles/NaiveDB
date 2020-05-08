package cn.edu.thssdb.exception;

public class TableNotExistException extends RuntimeException {
    private String key;

    public TableNotExistException()
    {
        super();
        this.key = null;
    }

    public TableNotExistException(String key)
    {
        super();
        this.key = key;
    }

    @Override
    public String getMessage() {
        if (key == null)
            return "Exception: table doesn't exist!";
        else
            return "Exception: table \"" + this.key + "\" doesn't exist!";
    }
}