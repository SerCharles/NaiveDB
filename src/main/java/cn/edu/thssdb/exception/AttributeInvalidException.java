package cn.edu.thssdb.exception;


/**
*描述：处理属性名称异常的情况，比如a.b.kebab
*参数：属性名称
*/
public class AttributeInvalidException extends RuntimeException{
    private String mName;
    public AttributeInvalidException(String name)
    {
        super();
        mName = name;
    }
    @Override
    public String getMessage() {
        return "Exception: Attribute " + mName + " is invalid!";
    }
}