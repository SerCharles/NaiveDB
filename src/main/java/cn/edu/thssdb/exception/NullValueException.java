package cn.edu.thssdb.exception;

/**
*描述：当非空列被插入空值时报错
 * 参数：列名
 */
public class NullValueException extends RuntimeException {
	private String mColumnName;
	
	public NullValueException(String column_name)
	{
		super();
		this.mColumnName = column_name;
	}
	
	@Override
	public String getMessage() {
		return "Exception: the column named " + mColumnName + " should not be null!";
	}
}
