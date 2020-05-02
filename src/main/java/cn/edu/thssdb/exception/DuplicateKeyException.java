package cn.edu.thssdb.exception;

public class DuplicateKeyException extends RuntimeException {
    private String key;

    public DuplicateKeyException()
    {
        super();
        key = null;
    }

    public DuplicateKeyException(String key)
    {
        super();
        this.key = key;
    }
    @Override
    public String getMessage() {
        if (key == null)
            return "Exception: insertion caused duplicated keys!";
        else
            return "Exception: insert key \"" + key + "\" caused duplicated keys!";
    }
}
