package cn.edu.thssdb.exception;

/**
*描述：处理属性名称冲突的情况，比如name同时在a，b里有还没指明白a.name还是b.name
*参数：属性名称
*/
public class AttributeCollisionException extends RuntimeException{
    private String mName;
    public AttributeCollisionException(String name)
    {
        super();
        mName = name;
    }
    @Override
    public String getMessage() {
        return "Exception: Attribute " + mName + " exists in more than one tables!\n"
                + "Try the format of TableName.AttributeName!";
    }
}
