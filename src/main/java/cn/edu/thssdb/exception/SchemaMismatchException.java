package cn.edu.thssdb.exception;

public class SchemaMismatchException extends RuntimeException {
    private String missingColumn;

    public SchemaMismatchException(String missingColumn)
    {
        super();
        this.missingColumn = missingColumn;
    }

    @Override
    public String getMessage() {
        return "Exception: expected column: " + missingColumn + " but not found.";
    }
}
