package  cn.edu.thssdb.parser;

import cn.edu.thssdb.exception.*;
import cn.edu.thssdb.schema.*;
import javafx.util.Pair;
import cn.edu.thssdb.query.*;
import cn.edu.thssdb.type.*;

import java.util.ArrayList;
import java.util.StringJoiner;

/**
 * 描述:sql语义分析类
 */
public class MyVisitor extends SQLBaseVisitor {
    private Manager manager;

    public MyVisitor(Manager manager) {
        super();
        this.manager = manager;
    }
    
    private Database GetCurrentDB() {
        Database current_base = manager.getCurrent();
        if(current_base == null) {
            throw new DatabaseNotExistException();
        }
        return current_base;
    }

    public String visitParse(SQLParser.ParseContext ctx) {
        return visitSql_stmt_list(ctx.sql_stmt_list());
    }

    public String visitSql_stmt_list(SQLParser.Sql_stmt_listContext ctx) {
        StringJoiner sj = new StringJoiner("\n\n");
        for (SQLParser.Sql_stmtContext subCtx : ctx.sql_stmt())
            sj.add(visitSql_stmt(subCtx));
        return sj.toString();
    }
    
    public String visitSql_stmt(SQLParser.Sql_stmtContext ctx) {
        if (ctx.create_table_stmt() != null)
            return visitCreate_table_stmt(ctx.create_table_stmt());
        if (ctx.create_db_stmt() != null)
            return visitCreate_db_stmt(ctx.create_db_stmt());
        if (ctx.drop_db_stmt() != null)
            return visitDrop_db_stmt(ctx.drop_db_stmt());
        if(ctx.show_meta_stmt() != null)
            return visitShow_meta_stmt(ctx.show_meta_stmt());
        if (ctx.delete_stmt() != null)
            return visitDelete_stmt(ctx.delete_stmt());
        if (ctx.drop_table_stmt() != null)
            return visitDrop_table_stmt(ctx.drop_table_stmt());
        if (ctx.insert_stmt() != null)
            return visitInsert_stmt(ctx.insert_stmt());
        if (ctx.select_stmt() != null)
            return visitSelect_stmt(ctx.select_stmt());
        if (ctx.use_db_stmt() != null)
            return visitUse_db_stmt(ctx.use_db_stmt());
        if (ctx.update_stmt() != null)
            return visitUpdate_stmt(ctx.update_stmt());
        if (ctx.show_table_stmt() != null)
            return visitShow_table_stmt(ctx.show_table_stmt());
        if (ctx.quit_stmt() != null)
            return visitQuit_stmt();
        return null;
    }
    
    /**
     * 描述：处理退出
     */
    private String visitQuit_stmt() {
        try {
            manager.quit();
        } catch (Exception e) {
            return e.getMessage();
        }
        return "Quited.";
    }
    
    /**
     * 描述：处理数据库建立
     */
    public String visitCreate_db_stmt(SQLParser.Create_db_stmtContext ctx) {
        String databaseName = ctx.database_name().getText();
        try {
            manager.createDatabaseIfNotExists(databaseName.toLowerCase());
        } catch (Exception e) {
            return e.getMessage();
        }
        return "Created database " + databaseName + ".";
    }
    
    /**
     * 描述：处理数据库删除
     */
    public String visitDrop_db_stmt(SQLParser.Drop_db_stmtContext ctx) {
        String name = ctx.database_name().getText();
        try {
            manager.deleteDatabase(name.toLowerCase());
        } catch (Exception e) {
            return e.getMessage();
        }
        return "Dropped database " + name + ".";
    }
    
    /**
     * 描述：处理数据库所有表显示
     */
    public String visitShow_table_stmt(SQLParser.Show_table_stmtContext ctx) {
        Database current_db = GetCurrentDB();
        try {
            return current_db.ToString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }
    
    /**
     * 描述：处理数据库切换
     */
    public String visitUse_db_stmt(SQLParser.Use_db_stmtContext ctx) {
        String name = ctx.database_name().getText();
        try {
            manager.switchDatabase(name.toLowerCase());
        } catch (Exception e) {
            return e.getMessage();
        }
        return "Switched to database " + name + ".";
    }
    
    /**
     * 描述：处理数据库建表
    */
    public String visitCreate_table_stmt(SQLParser.Create_table_stmtContext ctx) {
        String name = ctx.table_name().getText();
        int n = ctx.column_def().size();
        Column[] columns = new Column[n];
        int i = 0;
        
        //读取各个列的定义
        for (SQLParser.Column_defContext subCtx : ctx.column_def()) {
            columns[i++] = visitColumn_def(subCtx);
        }
        
        //读取表定义末端的信息--primary key
        if (ctx.table_constraint() != null) {
            String[] compositeNames = visitTable_constraint(ctx.table_constraint());
            
            for (String compositeName : compositeNames) {
                boolean found = false;
                for (Column c : columns) {
                    if (c.getName().toLowerCase().equals(compositeName.toLowerCase())) {
                        c.setPrimary(1);
                        found = true;
                    }
                }
                if (!found) {
                    throw new AttributeNotFoundException(compositeName);
                }
            }
        }
        
        //在当前数据库建表
        Database current_base = GetCurrentDB();
        try {
            manager.getCurrent().create(name.toLowerCase(), columns);
        } catch (Exception e) {
            return e.getMessage();
        }
        return "Created table " + name + ".";
    }
    
    /**
     * 描述：处理数据库删表
     */
    public String visitDrop_table_stmt(SQLParser.Drop_table_stmtContext ctx) {
        Database currentDB = GetCurrentDB();
        String name = ctx.table_name().getText();
        
        try {
            currentDB.drop(name.toLowerCase());
        } catch (Exception e) {
            return e.getMessage();
        }
        return "Dropped table " + name + ".";
    }
    
    /**
     * 描述：处理数据库单一表显示
     */
    public String visitShow_meta_stmt(SQLParser.Show_meta_stmtContext ctx) {
        String tableName = ctx.table_name().getText().toLowerCase();
        Database current_db = GetCurrentDB();
        try {
            return current_db.ShowOneTable(tableName);
        } catch (Exception e) {
            return e.getMessage();
        }
    }
    
    
    /**
     * 描述：处理插入元素
    */
    public String visitInsert_stmt(SQLParser.Insert_stmtContext ctx) {
        //table name
        String table_name = ctx.table_name().getText().toLowerCase();
        
        //column name处理
        String[] column_names = null;
        if (ctx.column_name() != null && ctx.column_name().size() != 0) {
            column_names = new String[ctx.column_name().size()];
            for (int i = 0; i < ctx.column_name().size(); i++)
                column_names[i] = ctx.column_name(i).getText().toLowerCase();
        }
        
        //value处理
        Database the_database = GetCurrentDB();
        for (SQLParser.Value_entryContext subCtx : ctx.value_entry()) {
            String[] values = visitValue_entry(subCtx);
            try {
                the_database.insert(table_name, column_names, values);
            } catch (Exception e) {
                return e.toString();
            }
        }
        return "Inserted " + ctx.value_entry().size() + " rows.";
    }
    
    /**
    * 描述：处理删除元素
     */
    public String visitDelete_stmt(SQLParser.Delete_stmtContext ctx) {
        Database the_database = GetCurrentDB();
        String table_name = ctx.table_name().getText().toLowerCase();
        if (ctx.K_WHERE() == null) {
            try {
                return the_database.delete(table_name, null);
            } catch (Exception e) {
                return e.toString();
            }
        }
        Logic logic = visitMultiple_condition(ctx.multiple_condition());
        try {
            return the_database.delete(table_name, logic);
        } catch (Exception e) {
            return e.toString();
        }
    }
    
    /**
     * 描述：处理更新元素
     */
    public String visitUpdate_stmt(SQLParser.Update_stmtContext ctx) {
        Database the_database = GetCurrentDB();
        String table_name = ctx.table_name().getText().toLowerCase();
        String column_name = ctx.column_name().getText().toLowerCase();
        Comparer value = visitExpression(ctx.expression());
        if (ctx.K_WHERE() == null) {
            try {
                return the_database.update(table_name, column_name, value, null);
            } catch (Exception e) {
                return e.toString();
            }
        }
        Logic logic = visitMultiple_condition(ctx.multiple_condition());
        try {
            return the_database.update(table_name, column_name, value, logic);
        } catch (Exception e) {
            return e.toString();
        }
    }
    

    /**
     * 描述：处理查询元素
     */
    public String visitSelect_stmt(SQLParser.Select_stmtContext ctx) {
        Database the_database = GetCurrentDB();
        boolean distinct = false;
        if (ctx.K_DISTINCT() != null)
            distinct = true;
        int column_count = ctx.result_column().size();
        String[] columns_selected = new String[column_count];
        //获取select的列名
        for (int i = 0; i < column_count; i++) {
            String column_name = ctx.result_column(i).getText().toLowerCase();
            if (column_name.equals("*")) {
                columns_selected = null;
                break;
            }
            columns_selected[i] = column_name;
        }
        
        //获取from的table，建立querytable
        int query_count = ctx.table_query().size();
        if(query_count == 0) {
            throw new NoSelectedTableException();
        }
        QueryTable the_query_table = null;
        try {
            the_query_table = visitTable_query(ctx.table_query(0));
        } catch (Exception e) {
            return e.toString();
        }
        if(the_query_table == null) {
            throw new NoSelectedTableException();
        }
        
        //建立逻辑，获得结果
        Logic logic = null;
        if (ctx.K_WHERE() != null)
            logic = visitMultiple_condition(ctx.multiple_condition());
        try {
            return the_database.select(columns_selected, the_query_table, logic, distinct);
        } catch (Exception e) {
            return e.toString();
        }
    }
    
    /**
     * 描述：读取输入的各种值
     */
    public String[] visitValue_entry(SQLParser.Value_entryContext ctx) {
        String[] values = new String[ctx.literal_value().size()];
        for (int i = 0; i < ctx.literal_value().size(); i++) {
            values[i] = ctx.literal_value(i).getText();
        }
        return values;
    }
    
    /**
     * 描述：读取列定义中的信息---名字，类型，是否主键，是否非空，最大长度
     */
    public Column visitColumn_def(SQLParser.Column_defContext ctx) {
        //读取当前列是否primary， not null
        boolean not_null = false;
        int primary = 0;
        for (SQLParser.Column_constraintContext subCtx : ctx.column_constraint()) {
            ConstraintType the_constraint_type = visitColumn_constraint(subCtx);
            if (the_constraint_type.equals(ConstraintType.PRIMARY)) {
                primary = 1;
            }
            else if (the_constraint_type.equals(ConstraintType.NOTNULL)) {
                not_null = true;
            }
            not_null = not_null || (primary == 1);
        }
        
        //列名称
        String name = ctx.column_name().getText().toLowerCase();
        
        //列类型
        Pair<ColumnType, Integer> type = visitType_name(ctx.type_name());
        ColumnType columnType = type.getKey();
        
        //最大长度（仅限string）
        int maxLength = type.getValue();
        return new Column(name, columnType, primary, not_null, maxLength);
    }

    /**
    * 描述：处理创建列时的type，max length
     */
    public Pair<ColumnType, Integer> visitType_name(SQLParser.Type_nameContext ctx) {
        if (ctx.T_INT() != null) {
            return new Pair<>(ColumnType.INT, -1);
        }
        if (ctx.T_LONG() != null) {
            return new Pair<>(ColumnType.LONG, -1);
        }
        if (ctx.T_FLOAT() != null) {
            return new Pair<>(ColumnType.FLOAT, -1);
        }
        if (ctx.T_DOUBLE() != null) {
            return new Pair<>(ColumnType.DOUBLE, -1);
        }
        if (ctx.T_STRING() != null) {
            try {
                //仅string返回值和最大长度
                return new Pair<>(ColumnType.STRING, Integer.parseInt(ctx.NUMERIC_LITERAL().getText()));
            } catch (Exception e) {
                throw new ValueFormatException();
            }
        }
        return null;
    }

    /**
     * 描述：返回列定义的限制--primary，notnull还是没有限制
     */
    public ConstraintType visitColumn_constraint(SQLParser.Column_constraintContext ctx) {
        if (ctx.K_PRIMARY() != null) {
            return ConstraintType.PRIMARY;
        }
        else if (ctx.K_NULL() != null) {
            return ConstraintType.NOTNULL;
        }
        return null;
    }
    
    /**
     * 描述：处理逻辑建立
     */
    public Condition visitCondition(SQLParser.ConditionContext ctx) {
        Comparer left = visitExpression(ctx.expression(0));
        Comparer right = visitExpression(ctx.expression(1));
        ConditionType type = visitComparator(ctx.comparator());
        return new Condition(left, right, type);
    }
    
    /**
     * 描述：得到6种比较符号
     */
    public ConditionType visitComparator(SQLParser.ComparatorContext ctx) {
        if (ctx.EQ() != null) {
            return ConditionType.EQ;
        }
        else if (ctx.NE() != null) {
            return ConditionType.NE;
        }
        else if (ctx.GT() != null) {
            return ConditionType.GT;
        }
        else if (ctx.LT() != null) {
            return ConditionType.LT;
        }
        else if (ctx.GE() != null) {
            return ConditionType.GE;
        }
        else if (ctx.LE() != null) {
            return ConditionType.LE;
        }
        return null;
    }
    
    /**
     * 描述：本应该是得到算术表达式，但是因为没有实现算术表达式，所以直接返回数值
     */
    public Comparer visitExpression(SQLParser.ExpressionContext ctx) {
        if (ctx.comparer() != null) {
            return visitComparer(ctx.comparer());
        }
        else {
            return null;
        }
    }
    
    /**
     *描述：读取一个comparer，值+类型
     */
    public Comparer visitComparer(SQLParser.ComparerContext ctx) {
        //处理column情况
        if (ctx.column_full_name() != null) {
            return new Comparer(ComparerType.COLUMN, ctx.column_full_name().getText());
        }
        //获得类型和内容
        LiteralType type = visitLiteral_value(ctx.literal_value());
        String text = ctx.literal_value().getText();
        switch (type) {
            case NUMBER:
                return new Comparer(ComparerType.NUMBER, text);
            case STRING:
                return new Comparer(ComparerType.STRING, text.substring(1, text.length() - 1));
            case NULL:
                return new Comparer(ComparerType.NULL, null);
            default:
                return null;
        }
    }
    
    /**
     *处理表定义的限制
     */
    public String[] visitTable_constraint(SQLParser.Table_constraintContext ctx) {
        int n = ctx.column_name().size();
        String[] composite_names = new String[n];
        for (int i = 0; i < n; i++) {
            composite_names[i] = ctx.column_name(i).getText().toLowerCase();
        }
        return composite_names;
    }
    
    /**
     *描述：获取querytable
     */
    public QueryTable visitTable_query(SQLParser.Table_queryContext ctx) {
        Database the_database = GetCurrentDB();
        //单一表
        if (ctx.K_JOIN().size() == 0) {
            return the_database.BuildSingleQueryTable(ctx.table_name(0).getText().toLowerCase());
        }
        //复合表，需要读取join逻辑
        else {
            Logic logic = visitMultiple_condition(ctx.multiple_condition());
            ArrayList<String> table_names = new ArrayList<>();
            for (SQLParser.Table_nameContext subCtx : ctx.table_name()) {
                table_names.add(subCtx.getText().toLowerCase());
            }
            return the_database.BuildJointQueryTable(table_names, logic);
        }
    }

    /**
    *描述：获取单一数值的类型
     */
    public LiteralType visitLiteral_value(SQLParser.Literal_valueContext ctx) {
        if (ctx.NUMERIC_LITERAL() != null) {
            return LiteralType.NUMBER;
        }
        if (ctx.STRING_LITERAL() != null) {
            return LiteralType.STRING;
        }
        if (ctx.K_NULL() != null) {
            return LiteralType.NULL;
        }
        return null;
    }
    
    /**
     *描述：处理复合逻辑
     */
    public Logic visitMultiple_condition(SQLParser.Multiple_conditionContext ctx) {
        //单一条件
        Object a = ctx.multiple_condition(0);
        Object b = ctx.AND();
        if (ctx.condition() != null)
            return new Logic(visitCondition(ctx.condition()));
        
        //复合逻辑
        LogicType logic_type;
        if (ctx.AND() != null) {
            logic_type = LogicType.AND;
        }
        else {
            logic_type = LogicType.OR;
        }
        return new Logic(visitMultiple_condition(ctx.multiple_condition(0)),
                visitMultiple_condition(ctx.multiple_condition(1)), logic_type);
    }
}