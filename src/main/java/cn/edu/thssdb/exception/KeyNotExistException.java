package cn.edu.thssdb.exception;

public class KeyNotExistException extends RuntimeException {
    private String key;

    public KeyNotExistException()
    {
        super();
        this.key = null;
    }

    public KeyNotExistException(String key)
    {
        super();
        this.key = key;
    }

    @Override
    public String getMessage() {
        if (key == null)
            return "Exception: key doesn't exist!";
        else
            return "Exception: key \"" + this.key + "\" doesn't exist!";
    }
}
