package cn.edu.thssdb.exception;

/**
*描述：处理插入元素时有重复元素的情况
 */
public class DuplicateColumnException extends RuntimeException {
	private String column;
	
	public DuplicateColumnException(String column)
	{
		super();
		this.column = column;
	}
	@Override
	public String getMessage() {
			return "Exception: insert key \"" + column + "\" caused duplicated keys!";
	}
}
