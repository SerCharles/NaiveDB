package cn.edu.thssdb.exception;

/**
 *描述：处理建表没有主键情况
 */
public class PrimaryNotExistException extends RuntimeException {
	private String name;
	
	public PrimaryNotExistException(String name)
	{
		super();
		this.name = name;
	}
	@Override
	public String getMessage() {
		return "Exception: there is no primary key in table " + name + "!";
	}
}
