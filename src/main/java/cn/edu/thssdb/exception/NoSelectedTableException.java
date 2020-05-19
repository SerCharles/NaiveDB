package cn.edu.thssdb.exception;

/**
 *描述：select没有选中任何table
 *参数：无
 */
public class NoSelectedTableException extends RuntimeException{
	public NoSelectedTableException()
	{
		super();
	}
	@Override
	public String getMessage() {
		return "Exception: You have not selected any table to search!";
	}
}