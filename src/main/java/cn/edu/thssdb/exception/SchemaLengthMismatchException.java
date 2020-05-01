package cn.edu.thssdb.exception;

public class SchemaLengthMismatchException extends RuntimeException {
    private int expectedLen;
    private int realLen;
    public SchemaLengthMismatchException(int expectedLen, int realLen)
    {
        super();
        this.expectedLen = expectedLen;
        this.realLen = realLen;
    }

    @Override
    public String getMessage() {
        return "Exception: expected " + expectedLen + " columns, " +
                "but got " + realLen + " columns.";
    }
}
