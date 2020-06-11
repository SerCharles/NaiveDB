package cn.edu.thssdb.query;

import cn.edu.thssdb.exception.AttributeCollisionException;
import cn.edu.thssdb.exception.AttributeInvalidException;
import cn.edu.thssdb.exception.AttributeNotFoundException;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.Row;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import cn.edu.thssdb.schema.Table;
import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.type.ComparerType;
import javafx.scene.control.Cell;

/**
 *描述：用于生成查询结果，包括对结果按照列进行选择，包括对distinct进行判重处理
 *参数：对应的querytable，选中的列，是否distinct
 */
public class QueryResult {
  private QueryTable mTable;
  private ArrayList<MetaInfo> mMetaInfoList;
  private boolean mWhetherDistinct;
  private HashSet<String> mHashSet;
  public boolean mWhetherRight;
  public String mErrorMessage;
  public ArrayList<Integer> mColumnIndex;
  public List<String> mColumnName;
  public ArrayList<Row> mResultList;
  
  //正常构造函数
  public QueryResult(QueryTable queryTable, String[] selectColumns, boolean whetherDistinct) {
    this.mTable = queryTable;
    this.mWhetherDistinct = whetherDistinct;
    this.mHashSet = new HashSet<>();
    mWhetherRight = true;
    mErrorMessage = "";
    this.mMetaInfoList = new ArrayList<MetaInfo>();
    this.mMetaInfoList.addAll(queryTable.GenerateMetaInfo());
    this.mResultList = new ArrayList<Row>();
    InitColumns(selectColumns);
  }
  
  //异常构造函数
  public QueryResult(String errorMessge) {
    mWhetherRight = false;
    mErrorMessage = errorMessge;
  }
  
  /**
   *描述：按照传入时候选中的列，初始化queryresult的各列信息
   *参数：选中的列
   *返回：无
   */
  private void InitColumns(String[] selectColumns) {
    this.mColumnIndex = new ArrayList<>();
    this.mColumnName = new ArrayList<>();
    
    //选中了一些列
    if (selectColumns != null) {
      for (String column_name : selectColumns) {
        this.mColumnIndex.add(GetColumnIndex(column_name));
        this.mColumnName.add(column_name);
      }
    }
    //没有选中任何列，那就全部返回
    else {
      int offset = 0;
      for (MetaInfo metaInfo : mMetaInfoList) {
        for (int i = 0; i < metaInfo.GetColumnSize(); i++) {
          String full_name = metaInfo.GetFullName(i);
          this.mColumnIndex.add(offset + i);
          this.mColumnName.add(full_name);
        }
        offset += metaInfo.GetColumnSize();
      }
    }
  }
  
  /**
   *描述：将选中的列（meta信息）转化为字符串输出
   *参数：无
   *返回：输出字符串
   */
  public String MetaToString() {
    String result = "";
    for(int i = 0; i < mColumnName.size(); i ++) {
      result += mColumnName.get(i);
      if(i != mColumnName.size() - 1) {
        result += ", ";
      }
    }
    return result;
  }

  
  /**
   *描述：将TableName.ColumnName形式的变量拆分出TableName,ColumnName
   *参数：全名
   *返回：一个长度为2的数组，0号是tablename，1号是columnname
   */
  private String[] SplitColumnName(String full_name) {
    String[] splited_name = full_name.split("\\.");
    if (splited_name.length != 2) {
      throw new AttributeInvalidException(full_name);
    }
    return splited_name;
  }
  
  /**
   *描述：找到一个列名在index对应的位置
   *参数：列名
   *返回：位置
   */
  public int GetColumnIndex(String column_name) {
    int index = 0;
    
    //只有columnname
    if (!column_name.contains(".")) {
      int equal_sum = 0;
      int total_index = 0;
      for (int i = 0; i < mMetaInfoList.size(); i++) {
        int current_index = mMetaInfoList.get(i).ColumnFind(column_name);
        if(current_index >= 0) {
          equal_sum ++;
          index = current_index + total_index;
        }
        total_index += mMetaInfoList.get(i).GetColumnSize();
      }
      if (equal_sum < 1) {
        throw new AttributeNotFoundException(column_name);
      }
      else if (equal_sum > 1) {
        throw new AttributeCollisionException(column_name);
      }
    }
    //tablename.columnname
    else {
      String[] splited_names = SplitColumnName(column_name);
      String table_name = splited_names[0];
      String entry_name = splited_names[1];
      boolean whether_find = false;
      int total_index = 0;
      for (int i = 0; i < mMetaInfoList.size(); i++) {
        String current_name = mMetaInfoList.get(i).GetTableName();
        if (!current_name.equals(table_name)) {
          total_index += mMetaInfoList.get(i).GetColumnSize();
          continue;
        }
  
        int current_index = mMetaInfoList.get(i).ColumnFind(entry_name);
        if (current_index >= 0) {
          whether_find = true;
          index = current_index + total_index;
          break;
        }
      }
      if(whether_find == false) {
        throw new AttributeNotFoundException(column_name);
      }
    }
    return index;
  }
  
  
  /**
   *描述：获取所有搜索结果
   *参数：无
   *返回：所有搜索结果，返回的每个row都是和mColumnName一一对应的，如果distinct还会用哈希判重
   */
  public void GenerateQueryRecords() {
    while(mTable.hasNext()) {
      JointRow new_row = mTable.next();
      if(new_row == null) {
        break;
      }
      Entry[] entries = new Entry[mColumnIndex.size()];
      ArrayList<Entry> full_entries = new_row.getEntries();
      for(int i = 0; i < mColumnIndex.size(); i ++) {
        int index = mColumnIndex.get(i);
        entries[i] = full_entries.get(index);
      }
      Row the_row = new Row(entries);
      String row_string = the_row.toString();
      if(!mWhetherDistinct || !mHashSet.contains(row_string)) {
        mResultList.add(the_row);
        if(mWhetherDistinct) {
          mHashSet.add(row_string);
        }
      }
    }
  }
}