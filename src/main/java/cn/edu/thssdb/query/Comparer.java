package cn.edu.thssdb.query;


import cn.edu.thssdb.type.ComparerType;

/**
*描述：比较元素类，用于两个元素的比较
*构造参数：type，string形式value
*/
public class Comparer {
    public ComparerType mType;
    public Comparable mValue;

    public Comparer(ComparerType type, String value) {
        this.mType = type;
        switch (type) {
            case NUMBER:
                this.mValue = Double.parseDouble(value);
                break;
            case STRING:
            case COLUMN:
                this.mValue = value;
                break;
            default:
                this.mValue = null;
        }
    }

}