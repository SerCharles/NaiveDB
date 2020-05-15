package cn.edu.thssdb.query;

import cn.edu.thssdb.type.ResultType;
import cn.edu.thssdb.type.LogicType;

/**
*描述：逻辑类，处理and/or连接的各个情况
 */
public class Logic {
    //如果logic是有多个and/or连接的
    public Logic mLeft;
    public Logic mRight;
    public LogicType mType;

    //如果logic仅有一个条件
    public Condition mCondition;
    public boolean mTerminal;

    public Logic(Logic left, Logic right, LogicType type) {
        this.mTerminal = false;
        this.mLeft = left;
        this.mRight = right;
        this.mType = type;
    }

    public Logic(Condition condition) {
        this.mTerminal = true;
        this.mCondition = condition;
    }

    /**
    *描述：计算当前逻辑的运算结果
    *参数：无
    *返回：结果类型
    */
    public ResultType GetResult(JointRow the_row) {
        //单一条件
        if(this.mTerminal == true)
        {
            if(this.mCondition == null)
            {
                return ResultType.TRUE;
            }
            return this.mCondition.GetResult(the_row);
        }
        //复合条件
        else
        {
            ResultType left_result = ResultType.TRUE;
            if(this.mLeft != null)
            {
                left_result = this.mLeft.GetResult(the_row);
            }
            ResultType right_result = ResultType.TRUE;
            if(this.mRight != null)
            {
                right_result = this.mRight.GetResult(the_row);
            }
            if(mType == LogicType.AND)
            {
                //一共9个条件
                //false 和三种情况一起都是false，覆盖了5条件
                if(left_result == ResultType.FALSE || right_result == ResultType.FALSE)
                {
                    return ResultType.FALSE;
                }
                //true，unknown和unknown一起都是unknown，覆盖了3条件
                else if(left_result == ResultType.UNKNOWN || right_result == ResultType.UNKNOWN)
                {
                    return ResultType.UNKNOWN;
                }
                //true和true一起是true，1条件
                else if(left_result == ResultType.TRUE && right_result == ResultType.TRUE)
                {
                    return ResultType.TRUE;
                }
            }
            else if(mType == LogicType.OR)
            {
                //一共9个条件
                //true 和三种情况一起都是true，覆盖了5条件
                if(left_result == ResultType.TRUE || right_result == ResultType.TRUE)
                {
                    return ResultType.TRUE;
                }
                //false，unknown和unknown一起都是unknown，覆盖了3条件
                else if(left_result == ResultType.UNKNOWN || right_result == ResultType.UNKNOWN)
                {
                    return ResultType.UNKNOWN;
                }
                //false和false一起是false，1条件
                else if(left_result == ResultType.FALSE && right_result == ResultType.FALSE)
                {
                    return ResultType.FALSE;
                }
            }
        }
        return ResultType.UNKNOWN;
    }
}
