package cn.edu.thssdb.exception;

/**
*描述：未知异常
 * 参数：未知
 */
public class OtherException extends RuntimeException{
	public OtherException()
	{
		super();
	}
	@Override
	public String getMessage() {
		return "Unknown error!";
	}
}
