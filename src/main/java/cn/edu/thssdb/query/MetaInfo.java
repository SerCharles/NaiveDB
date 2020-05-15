package cn.edu.thssdb.query;

import cn.edu.thssdb.schema.Column;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述：元数据类型
 * 构造参数：表名，表的列名
*/
class MetaInfo {

  private String tableName;
  private List<Column> columns;

  MetaInfo(String tableName, ArrayList<Column> columns) {
    this.tableName = tableName;
    this.columns = columns;
  }
  
  /**
   * 描述：找到对应列的位置
   * 参数：列名
   * 返回：位置i
   */
  int ColumnFind(String name) {
    for(int i = 0; i < columns.size(); i ++) {
      if(columns.get(i).getName().equals(name)){
        return i;
      }
    }
    return -1;
  }
  
  /**
   * 描述：返回对应列全名
   * 参数：列index
   * 返回：全名，tablename.attrname
   */
  String GetFullName(int index) {
    if(index < 0 || index >= columns.size()) {
      return null;
    }
    String name = tableName + "." + columns.get(index).getName();
    return name;
  }
  
  int GetColumnSize() {
    return columns.size();
  }
  String GetTableName() {
    return tableName;
  }
}