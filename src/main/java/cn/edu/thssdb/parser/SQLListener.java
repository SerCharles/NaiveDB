// Generated from /Users/anyanzhe/workspace/ThssDB/src/main/java/cn/edu/thssdb/parser/SQL.g4 by ANTLR 4.8
package cn.edu.thssdb.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SQLParser}.
 */
public interface SQLListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SQLParser#parse}.
	 * @param ctx the parse tree
	 */
	void enterParse(SQLParser.ParseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#parse}.
	 * @param ctx the parse tree
	 */
	void exitParse(SQLParser.ParseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#sql_stmt_list}.
	 * @param ctx the parse tree
	 */
	void enterSql_stmt_list(SQLParser.Sql_stmt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#sql_stmt_list}.
	 * @param ctx the parse tree
	 */
	void exitSql_stmt_list(SQLParser.Sql_stmt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#sql_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSql_stmt(SQLParser.Sql_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#sql_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSql_stmt(SQLParser.Sql_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#create_db_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_db_stmt(SQLParser.Create_db_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#create_db_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_db_stmt(SQLParser.Create_db_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#drop_db_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_db_stmt(SQLParser.Drop_db_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#drop_db_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_db_stmt(SQLParser.Drop_db_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#create_user_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_user_stmt(SQLParser.Create_user_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#create_user_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_user_stmt(SQLParser.Create_user_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#drop_user_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_user_stmt(SQLParser.Drop_user_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#drop_user_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_user_stmt(SQLParser.Drop_user_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#create_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table_stmt(SQLParser.Create_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#create_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table_stmt(SQLParser.Create_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#show_meta_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_meta_stmt(SQLParser.Show_meta_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#show_meta_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_meta_stmt(SQLParser.Show_meta_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#grant_stmt}.
	 * @param ctx the parse tree
	 */
	void enterGrant_stmt(SQLParser.Grant_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#grant_stmt}.
	 * @param ctx the parse tree
	 */
	void exitGrant_stmt(SQLParser.Grant_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#revoke_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_stmt(SQLParser.Revoke_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#revoke_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_stmt(SQLParser.Revoke_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#use_db_stmt}.
	 * @param ctx the parse tree
	 */
	void enterUse_db_stmt(SQLParser.Use_db_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#use_db_stmt}.
	 * @param ctx the parse tree
	 */
	void exitUse_db_stmt(SQLParser.Use_db_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#delete_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDelete_stmt(SQLParser.Delete_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#delete_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDelete_stmt(SQLParser.Delete_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#drop_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_table_stmt(SQLParser.Drop_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#drop_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_table_stmt(SQLParser.Drop_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#show_db_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_db_stmt(SQLParser.Show_db_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#show_db_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_db_stmt(SQLParser.Show_db_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#quit_stmt}.
	 * @param ctx the parse tree
	 */
	void enterQuit_stmt(SQLParser.Quit_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#quit_stmt}.
	 * @param ctx the parse tree
	 */
	void exitQuit_stmt(SQLParser.Quit_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#show_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_table_stmt(SQLParser.Show_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#show_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_table_stmt(SQLParser.Show_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#insert_stmt}.
	 * @param ctx the parse tree
	 */
	void enterInsert_stmt(SQLParser.Insert_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#insert_stmt}.
	 * @param ctx the parse tree
	 */
	void exitInsert_stmt(SQLParser.Insert_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#value_entry}.
	 * @param ctx the parse tree
	 */
	void enterValue_entry(SQLParser.Value_entryContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#value_entry}.
	 * @param ctx the parse tree
	 */
	void exitValue_entry(SQLParser.Value_entryContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#select_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSelect_stmt(SQLParser.Select_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#select_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSelect_stmt(SQLParser.Select_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#create_view_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_view_stmt(SQLParser.Create_view_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#create_view_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_view_stmt(SQLParser.Create_view_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#drop_view_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_view_stmt(SQLParser.Drop_view_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#drop_view_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_view_stmt(SQLParser.Drop_view_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#update_stmt}.
	 * @param ctx the parse tree
	 */
	void enterUpdate_stmt(SQLParser.Update_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#update_stmt}.
	 * @param ctx the parse tree
	 */
	void exitUpdate_stmt(SQLParser.Update_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#column_def}.
	 * @param ctx the parse tree
	 */
	void enterColumn_def(SQLParser.Column_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#column_def}.
	 * @param ctx the parse tree
	 */
	void exitColumn_def(SQLParser.Column_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#type_name}.
	 * @param ctx the parse tree
	 */
	void enterType_name(SQLParser.Type_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#type_name}.
	 * @param ctx the parse tree
	 */
	void exitType_name(SQLParser.Type_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#column_constraint}.
	 * @param ctx the parse tree
	 */
	void enterColumn_constraint(SQLParser.Column_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#column_constraint}.
	 * @param ctx the parse tree
	 */
	void exitColumn_constraint(SQLParser.Column_constraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#multiple_condition}.
	 * @param ctx the parse tree
	 */
	void enterMultiple_condition(SQLParser.Multiple_conditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#multiple_condition}.
	 * @param ctx the parse tree
	 */
	void exitMultiple_condition(SQLParser.Multiple_conditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#condition}.
	 * @param ctx the parse tree
	 */
	void enterCondition(SQLParser.ConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#condition}.
	 * @param ctx the parse tree
	 */
	void exitCondition(SQLParser.ConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#comparer}.
	 * @param ctx the parse tree
	 */
	void enterComparer(SQLParser.ComparerContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#comparer}.
	 * @param ctx the parse tree
	 */
	void exitComparer(SQLParser.ComparerContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#comparator}.
	 * @param ctx the parse tree
	 */
	void enterComparator(SQLParser.ComparatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#comparator}.
	 * @param ctx the parse tree
	 */
	void exitComparator(SQLParser.ComparatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(SQLParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(SQLParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#table_constraint}.
	 * @param ctx the parse tree
	 */
	void enterTable_constraint(SQLParser.Table_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#table_constraint}.
	 * @param ctx the parse tree
	 */
	void exitTable_constraint(SQLParser.Table_constraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#result_column}.
	 * @param ctx the parse tree
	 */
	void enterResult_column(SQLParser.Result_columnContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#result_column}.
	 * @param ctx the parse tree
	 */
	void exitResult_column(SQLParser.Result_columnContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#table_query}.
	 * @param ctx the parse tree
	 */
	void enterTable_query(SQLParser.Table_queryContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#table_query}.
	 * @param ctx the parse tree
	 */
	void exitTable_query(SQLParser.Table_queryContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#auth_level}.
	 * @param ctx the parse tree
	 */
	void enterAuth_level(SQLParser.Auth_levelContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#auth_level}.
	 * @param ctx the parse tree
	 */
	void exitAuth_level(SQLParser.Auth_levelContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#literal_value}.
	 * @param ctx the parse tree
	 */
	void enterLiteral_value(SQLParser.Literal_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#literal_value}.
	 * @param ctx the parse tree
	 */
	void exitLiteral_value(SQLParser.Literal_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#column_full_name}.
	 * @param ctx the parse tree
	 */
	void enterColumn_full_name(SQLParser.Column_full_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#column_full_name}.
	 * @param ctx the parse tree
	 */
	void exitColumn_full_name(SQLParser.Column_full_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#database_name}.
	 * @param ctx the parse tree
	 */
	void enterDatabase_name(SQLParser.Database_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#database_name}.
	 * @param ctx the parse tree
	 */
	void exitDatabase_name(SQLParser.Database_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#table_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_name(SQLParser.Table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#table_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_name(SQLParser.Table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#user_name}.
	 * @param ctx the parse tree
	 */
	void enterUser_name(SQLParser.User_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#user_name}.
	 * @param ctx the parse tree
	 */
	void exitUser_name(SQLParser.User_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#column_name}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name(SQLParser.Column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#column_name}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name(SQLParser.Column_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#view_name}.
	 * @param ctx the parse tree
	 */
	void enterView_name(SQLParser.View_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#view_name}.
	 * @param ctx the parse tree
	 */
	void exitView_name(SQLParser.View_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#password}.
	 * @param ctx the parse tree
	 */
	void enterPassword(SQLParser.PasswordContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#password}.
	 * @param ctx the parse tree
	 */
	void exitPassword(SQLParser.PasswordContext ctx);
}