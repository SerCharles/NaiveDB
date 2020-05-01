package cn.edu.thssdb.exception;

public class KeyNotExistException extends RuntimeException {
    private String key;

    public KeyNotExistException()
    {
        super();
        this.key = "";
    }

    public KeyNotExistException(String key)
    {
        super();
        this.key = key;
        if(key == null)
            this.key = "null";
    }

    @Override
    public String getMessage() {
        return "Exception: key \"" + this.key + "\" doesn't exist!";
    }
}
