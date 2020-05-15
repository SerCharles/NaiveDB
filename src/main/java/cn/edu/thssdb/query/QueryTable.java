package cn.edu.thssdb.query;


import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Row;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;


/**
*描述：querytable父类
*构造函数：无
 */
public abstract class QueryTable implements Iterator<Row> {
	LinkedList<JointRow> mQueue;
	Logic mLogicSelect;
	boolean isFirst;
	public ArrayList<Column> mColumns;
	
	public abstract void PrepareNext();
	
	public abstract ArrayList<MetaInfo> GenerateMetaInfo();
	
	QueryTable() {
		this.mQueue = new LinkedList<>();
		this.isFirst = true;
	}
	
	/**
	*描述：设置选择逻辑
	*参数：选择逻辑
	*返回：无
	*/
	public void SetLogicSelect(Logic selectLogic) {
		this.mLogicSelect = selectLogic;
	}
	
	/**
	*描述：判断是否还有元素
	*参数：无
	*返回：无
    */
	@Override
	public boolean hasNext() {
		return isFirst || !mQueue.isEmpty();
	}
	
	/**
	*描述：返回下一个符合条件的元素，同时更新队列，保证除了自身之外非空
	*参数：无
	*返回：下一个元素row
	 */
	@Override
	public JointRow next() {
		if (mQueue.isEmpty()) {
			PrepareNext();
			if(isFirst) {
				isFirst = false;
			}
		}
		JointRow result = null;
		if(!mQueue.isEmpty()) {
			result = mQueue.poll();
		}
		else
		{
			return null;
		}
		if (mQueue.isEmpty()) {
			PrepareNext();
		}
		return result;
	}
}