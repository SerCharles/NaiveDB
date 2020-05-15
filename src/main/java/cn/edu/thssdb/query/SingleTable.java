package cn.edu.thssdb.query;

import java.util.ArrayList;
import java.util.Iterator;

import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Table;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.type.ComparerType;
import cn.edu.thssdb.type.ConditionType;
import cn.edu.thssdb.type.ResultType;

/**
 * 描述：单一table使用的查找表
 * 参数：table
 */

public class SingleTable extends QueryTable implements Iterator<Row> {
	private Table mTable;
	private Iterator<Row> mIterator;
	
	public SingleTable(Table table) {
		super();
		this.mTable = table;
		this.mIterator = table.iterator();
		this.mColumns = table.columns;
	}
	
	/**
	 * 描述：返回元数据信息
	 * 参数：无
	 * 返回：元数据信息
	 */
	@Override
	public ArrayList<MetaInfo> GenerateMetaInfo() {
		ArrayList<MetaInfo> the_meta = new ArrayList<>();
		the_meta.add(new MetaInfo(mTable.tableName, mTable.columns));
		return the_meta;
	}
	
	/**
	 * 描述：找到下一个符合条件的，放到队列里
	 * 参数：无
	 * 返回：无
	 */
	@Override
	public void PrepareNext() {
		//判断空
		if (mLogicSelect == null) {
			PrepareNextDirect();
			return;
		} else if (mLogicSelect.mTerminal == true) {
			Condition the_condition = mLogicSelect.mCondition;
			if (the_condition == null) {
				PrepareNextDirect();
				return;
			} else {
				Comparer left_comparer = the_condition.mLeft;
				Comparer right_comparer = the_condition.mRight;
				//condition是常量比较
				if (left_comparer.mType != ComparerType.COLUMN && right_comparer.mType != ComparerType.COLUMN) {
					ResultType the_result = the_condition.GetResult();
					if (the_result == ResultType.TRUE) {
						PrepareNextDirect();
						return;
					} else {
						return;
					}
				}
				//仅有一个变量，而且是唯一主键，而且是等于,而且第一次
				else if (left_comparer.mType == ComparerType.COLUMN && right_comparer.mType != ComparerType.COLUMN
						&& the_condition.mType == ConditionType.EQ) {
					String primary_key_name = mTable.GetPrimaryName();
					if (primary_key_name.equals(left_comparer.mValue)) {
						Comparable const_value = right_comparer.mValue;
						if(const_value != null) {
							PrepareNextByCache(const_value);
							return;
						}
						return;
					}
				} else if (right_comparer.mType == ComparerType.COLUMN && left_comparer.mType != ComparerType.COLUMN
						&& the_condition.mType == ConditionType.EQ) {
					String primary_key_name = mTable.GetPrimaryName();
					if (primary_key_name.equals(right_comparer.mValue)) {
						Comparable const_value = left_comparer.mValue;
						if(const_value != null) {
							PrepareNextByCache(const_value);
							return;
						}
					}
				}
			}
		}
		//其余情况，直接按照逻辑查找
		PrepareNextByLogic();
	}
	
	/**
	 * 描述：直接添加下一个
	 * 参数：无
	 * 返回：无
	 */
	private void PrepareNextDirect() {
		if (mIterator.hasNext()) {
			JointRow the_row = new JointRow(mIterator.next(), mTable);
			mQueue.add(the_row);
		}
	}
	
	/**
	 * 描述：用table的cache机制加速查找，仅限查找主键，单一逻辑，=的情况
	 * 参数：待查找的主键value
	 * 返回：无
	 */
	private void PrepareNextByCache(Comparable const_value) {
		if(!isFirst) {
			return;
		}
		Row row = mTable.get(new Entry(const_value));
		JointRow the_row = new JointRow(row, mTable);
		mQueue.add(the_row);
	}
	
	/**
	 * 描述：对于一般情况的查找
	 * 参数：待查找的主键value
	 * 返回：无
	 */
	private void PrepareNextByLogic() {
		while (mIterator.hasNext()) {
			Row row = mIterator.next();
			JointRow search_row = new JointRow(row, mTable);
			if (mLogicSelect.GetResult(search_row) != ResultType.TRUE) {
				continue;
			}
			mQueue.add(search_row);
			break;
		}
	}
	
}
