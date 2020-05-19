package cn.edu.thssdb.exception;

public class ValueFormatException extends RuntimeException {
	
	@Override
	public String getMessage() {
			return "Exception: Value format mismatched!";
		}
}
