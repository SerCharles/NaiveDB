package cn.edu.thssdb.exception;

/**
 *描述：未知异常
 *参数：未知
 */
public class NotImplementedException extends RuntimeException{
	public NotImplementedException()
	{
		super();
	}
	@Override
	public String getMessage() {
		return "Exception: Not implemented!";
	}
}
