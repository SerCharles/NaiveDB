package cn.edu.thssdb.query;

import java.util.ArrayList;
import java.util.LinkedList;

import cn.edu.thssdb.exception.AttributeCollisionException;
import cn.edu.thssdb.exception.AttributeInvalidException;
import cn.edu.thssdb.exception.AttributeNotFoundException;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.Table;
import cn.edu.thssdb.type.ComparerType;
import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.query.Comparer;

/**
*描述：查询用的row，用于处理查询时的值读取等
*参数：如果是单一table，就只需要单一row和table即可
*如果是复合table，那么就是两个长度一样的linkedlist，一个是row，一个是table
*/

public class JointRow extends Row {
    private ArrayList<Table> mTableInfoList;
    public JointRow(LinkedList<Row> rows, ArrayList<Table> tables) {
        super();
        mTableInfoList = new ArrayList<>();
        this.entries = new ArrayList<>();
        //for (int i = rows.size() - 1; i >= 0; i--) {
        for (int i = rows.size() - 1; i >= 0; i--) {
            entries.addAll(rows.get(i).getEntries());
        }
        for(Table table : tables){
            mTableInfoList.add(table);
        }
    }
    public JointRow(Row the_row, Table the_table) {
        super();
        mTableInfoList = new ArrayList<>();
        this.entries = new ArrayList<>();
        entries.addAll(the_row.getEntries());
        mTableInfoList.add(the_table);
    }

    /**
    *描述：将columntype转换成comparertype
    *参数：columntype
    *返回：comparertype
     */
    private ComparerType GetComparerType(ColumnType the_type) {
        switch (the_type) {
            case LONG:
            case FLOAT:
            case INT:
            case DOUBLE:
                return ComparerType.NUMBER;
            case STRING:
                return ComparerType.STRING;
        }
        return ComparerType.NULL;
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
    *描述：给定列，返回对应取值对应的comparer
    *参数：全名
    *返回：取值
    */
    public Comparer getColumnComparer(String column_name) {
        int index = 0;
        ColumnType column_type = ColumnType.INT;
        ComparerType comparer_type = ComparerType.NULL;

        //只有columnname
        if (!column_name.contains(".")) {
            int equal_sum = 0;
            int total_index = 0;
            for (int i = 0; i < mTableInfoList.size(); i++) {
                Table the_table = mTableInfoList.get(i);

                for (int j = 0; j < the_table.columns.size(); j++) {
                    if (column_name.equals(the_table.columns.get(j).getName())) {
                        equal_sum++;
                        index = total_index + j;
                        column_type = the_table.columns.get(j).getType();
                    }
                }
                total_index += the_table.columns.size();
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
            int total_index = 0;
            boolean whether_found = false;
            for (Table table : mTableInfoList) {
                if (table_name.equals(table.tableName)) {
                    for(int j = 0; j < table.columns.size(); j ++){
                        if(entry_name.equals(table.columns.get(j).getName())){
                            whether_found = true;
                            index = total_index + j;
                            column_type = table.columns.get(j).getType();
                            break;
                        }
                    }
                    break;
                }
                total_index += table.columns.size();
            }
            if (!whether_found) {
                throw new AttributeNotFoundException(column_name);
            }
        }
        comparer_type = GetComparerType(column_type);
        Comparable comparer_value = this.entries.get(index).value;
        
        if(comparer_value == null){
            return new Comparer(ComparerType.NULL, null);
        }
        Comparer the_comparer = new Comparer(comparer_type, "" + comparer_value);
        return the_comparer;
    }
}


