package cn.edu.thssdb.exception;

public class DuplicateTableException extends RuntimeException {
    private String tableName;

    public DuplicateTableException() {
        super();
        tableName = null;
    }

    public DuplicateTableException(String name) {
        super();
        tableName = name;
    }

    @Override
    public String getMessage() {
        if (tableName == null)
            return "Exception: create table caused duplicated tables!";
        else
            return "Exception: create table \"" + tableName + "\" caused duplicated tables!";
    }
}
