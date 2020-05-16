package cn.edu.thssdb.exception;

/**
 *描述：当string类型列长度超过max length报错
 * 参数：列名
 */
public class ValueLengthExceedException extends RuntimeException {
	private String mColumnName;
	
	public ValueLengthExceedException(String column_name)
	{
		super();
		this.mColumnName = column_name;
	}
	
	@Override
	public String getMessage() {
		return "Exception: the column named " + mColumnName + "'s length has exceeded its maximum!";
	}
}
