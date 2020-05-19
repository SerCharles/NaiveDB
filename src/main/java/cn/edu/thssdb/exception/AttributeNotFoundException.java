package cn.edu.thssdb.exception;

/**
*描述：处理属性名称不存在的情况
*参数：属性名称
*/
public class AttributeNotFoundException extends RuntimeException{
    private String mName;
    public AttributeNotFoundException(String name)
    {
        super();
        mName = name;
    }
    @Override
    public String getMessage() {
        return "Exception: Attribute " + mName + " does not exist!";
    }
}