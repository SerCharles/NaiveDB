package cn.edu.thssdb.exception;

import cn.edu.thssdb.type.ComparerType;

/**
*描述：待比较参数类型不匹配异常
*参数：两个类型
 */
public class TypeNotMatchException extends RuntimeException {

    private ComparerType mType1;
    private ComparerType mType2;
    public TypeNotMatchException(ComparerType type1, ComparerType type2)
    {
        super();
        mType1 = type1;
        mType2 = type2;
    }
    @Override
    public String getMessage() {
        String message1 = "Null";
        String message2 = "Null";
        switch(this.mType1) {
            case COLUMN:
                message1 = "Column";
                break;
            case STRING:
                message1 = "String";
                break;
            case NUMBER:
                message1 = "Number";
                break;
            case NULL:
                message1 = "Null";
                break;
        }
        switch(this.mType2) {
            case COLUMN:
                message2 = "Column";
                break;
            case STRING:
                message2 = "String";
                break;
            case NUMBER:
                message2 = "Number";
                break;
            case NULL:
                message2 = "Null";
                break;
        }
        return "Exception: Type 1 " + message1 + " and " + "type 2 " + message2 + " do not match!";
    }

}
