package cn.edu.thssdb.exception;

/**
 * 描述：未知异常
 * 参数：无
 */
public class OtherException extends RuntimeException{
	public OtherException()
	{
		super();
	}
	@Override
	public String getMessage() {
		return "Exception: Unknown error!";
	}
}
