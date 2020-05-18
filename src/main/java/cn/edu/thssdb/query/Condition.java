package cn.edu.thssdb.query;

import cn.edu.thssdb.exception.FileIOException;
import cn.edu.thssdb.exception.TypeNotMatchException;
import cn.edu.thssdb.exception.OtherException;
import cn.edu.thssdb.type.ComparerType;
import cn.edu.thssdb.type.ConditionType;
import cn.edu.thssdb.type.ResultType;
import cn.edu.thssdb.query.JointRow;

import static cn.edu.thssdb.utils.Global.DATA_DIRECTORY;

/**
*描述：处理一个逻辑比较式子
*构造参数：左右comparer，比较类型
 */
public class Condition {
    Comparer mLeft;
    Comparer mRight;
    ConditionType mType;
    public Condition(Comparer left, Comparer right, ConditionType type) {
        this.mLeft = left;
        this.mRight = right;
        this.mType = type;
    }

    /**
    *描述：计算当前条件的运算结果
    *参数：无
    *返回：结果类型
     */
    public ResultType GetResult(JointRow the_row) {
        //null和啥比较都是unknown
        if(mLeft.mType == ComparerType.NULL || mRight.mType == ComparerType.NULL
                || mLeft == null || mRight == null || mLeft.mValue == null || mRight.mValue == null)
        {
            return ResultType.UNKNOWN;
        }
        else
        {
            Comparable value_left = mLeft.mValue;
            Comparable value_right = mRight.mValue;
            ComparerType type_left = mLeft.mType;
            ComparerType type_right = mRight.mType;
            if(mLeft.mType == ComparerType.COLUMN)
            {
                Comparer left_comparer = the_row.getColumnComparer((String)mLeft.mValue);
                value_left = left_comparer.mValue;
                type_left = left_comparer.mType;
            }
            if(mRight.mType == ComparerType.COLUMN)
            {
                Comparer right_comparer = the_row.getColumnComparer((String)mRight.mValue);
                value_right = right_comparer.mValue;
                type_right = right_comparer.mType;
            }
            
            //null不判断
            if(type_left == ComparerType.NULL || type_right == ComparerType.NULL || value_left == null || value_right == null) {
                return ResultType.UNKNOWN;
            }
            
            //待比较的类型不一样，报错
            if(type_left != type_right)
            {
                throw new TypeNotMatchException(type_left, type_right);
            }
            else
            {
                //比较
                boolean result = false;
                switch(mType){
                    case EQ :
                        result = (value_left.compareTo(value_right) == 0);
                        break;
                    case NE :
                        result = (value_left.compareTo(value_right) != 0);
                        break;
                    case GT :
                        result = (value_left.compareTo(value_right) > 0);
                        break;
                    case LT :
                        result = (value_left.compareTo(value_right) < 0);
                        break;
                    case GE :
                        result = (value_left.compareTo(value_right) >= 0);
                        break;
                    case LE :
                        result = (value_left.compareTo(value_right) <= 0);
                        break;
                }
                if(result == true)
                {
                    return ResultType.TRUE;
                }
                else
                {
                    return ResultType.FALSE;
                }
            }
        }
        //return ResultType.UNKNOWN;
    }
    
    /**
     *描述：计算当前条件的运算结果，仅限左右都不是column的情况
     *参数：无
     *返回：结果类型
     */
    public ResultType GetResult() {
        //null和啥比较都是unknown
        if(mLeft.mType == ComparerType.NULL || mRight.mType == ComparerType.NULL
                || mLeft == null || mRight == null || mLeft.mValue == null || mRight.mValue == null)
        {
            return ResultType.UNKNOWN;
        }
        else
        {
            Comparable value_left = mLeft.mValue;
            Comparable value_right = mRight.mValue;
            ComparerType type_left = mLeft.mType;
            ComparerType type_right = mRight.mType;
            if(mLeft.mType == ComparerType.COLUMN)
            {
                throw new OtherException();
            }
            if(mRight.mType == ComparerType.COLUMN)
            {
                throw new OtherException();
            }
            //待比较的类型不一样，报错
            if(type_left != type_right)
            {
                throw new TypeNotMatchException(type_left, type_right);
            }
            else
            {
                //比较
                boolean result = false;
                switch(mType){
                    case EQ :
                        result = (value_left.compareTo(value_right) == 0);
                        break;
                    case NE :
                        result = (value_left.compareTo(value_right) != 0);
                        break;
                    case GT :
                        result = (value_left.compareTo(value_right) > 0);
                        break;
                    case LT :
                        result = (value_left.compareTo(value_right) < 0);
                        break;
                    case GE :
                        result = (value_left.compareTo(value_right) >= 0);
                        break;
                    case LE :
                        result = (value_left.compareTo(value_right) <= 0);
                        break;
                }
                if(result == true)
                {
                    return ResultType.TRUE;
                }
                else
                {
                    return ResultType.FALSE;
                }
            }
        }
        //return ResultType.UNKNOWN;
    }
}
