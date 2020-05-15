package cn.edu.thssdb.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Table;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.type.ComparerType;
import cn.edu.thssdb.type.ConditionType;
import cn.edu.thssdb.type.ResultType;
/**
 * 描述：多个table使用的联合查找表
 * 参数：table
 */
public class JointTable extends QueryTable implements Iterator<Row>{
	
	private ArrayList<Iterator<Row>> mIterators;
	private ArrayList<Table> mTables;
	private Logic mLogicJoin;
	
	/**长度=每个table，分别代表每个table要出来join的列*/
	private LinkedList<Row> mRowsToBeJoined;
	
	public JointTable(ArrayList<Table> tables, Logic joinLogic) {
		super();
		this.mTables = tables;
		this.mIterators = new ArrayList<>();
		this.mRowsToBeJoined = new LinkedList<>();
		this.mLogicJoin = joinLogic;
		this.mColumns = new ArrayList<>();
		for (Table t : tables) {
			this.mColumns.addAll(t.columns);
			this.mIterators.add(t.iterator());
		}
	}
	
	
	/**
	 * 描述：返回元数据信息
	 * 参数：无
	 * 返回：元数据信息
	 */
	@Override
	public ArrayList<MetaInfo> GenerateMetaInfo() {
		ArrayList<MetaInfo> the_meta = new ArrayList<>();
		for (Table table : mTables) {
			the_meta.add(new MetaInfo(table.tableName, table.columns));
		}
		return the_meta;
	}
	
	
	/**
	 * 描述：找到下一个符合条件的，放到队列里
	 * 参数：无
	 * 返回：无
	 */
	@Override
	public void PrepareNext() {
		while (true) {
			JointRow the_row = JoinRows();
			if (the_row == null) {
				return;
			}
			
			if(mLogicJoin == null || mLogicJoin.GetResult(the_row) == ResultType.TRUE) {
				if(mLogicSelect == null || mLogicSelect.GetResult(the_row) == ResultType.TRUE) {
					mQueue.add(the_row);
					return;
				}
			}
		}
	}
	
	/**
	* 描述：将下一组row连接成一个完整的row，用于判断
	* 参数：无
	* 返回：无
	 */
	private JointRow JoinRows() {
		if (mRowsToBeJoined.isEmpty()) {
			for (Iterator<Row> iter : mIterators) {
				if (!iter.hasNext()) {
					return null;
				}
				mRowsToBeJoined.push(iter.next());
			}
			return new JointRow(mRowsToBeJoined, mTables);
		} else {
			int index;
			for (index = mIterators.size() - 1; index >= 0; index--) {
				//类似加法进位一样的机制：一直重新设置iterator，类似进位，直到有一个iterator有能进位的为止
				mRowsToBeJoined.pop();
				if (!mIterators.get(index).hasNext()) {
					mIterators.set(index, mTables.get(index).iterator());
				}
				else {
					break;
				}
			}
			if (index < 0) {
				return null;
			}
			//再补回去
			for (int i = index; i < mIterators.size(); i++) {
				if (!mIterators.get(i).hasNext())
					return null;
				mRowsToBeJoined.push(mIterators.get(i).next());
			}
			return new JointRow(mRowsToBeJoined, mTables);
		}
	}

	
}
