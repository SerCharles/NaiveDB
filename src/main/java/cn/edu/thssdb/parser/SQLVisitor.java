// Generated from /Users/anyanzhe/workspace/ThssDB/src/main/java/cn/edu/thssdb/parser/SQL.g4 by ANTLR 4.8
package cn.edu.thssdb.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SQLParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface SQLVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link SQLParser#parse}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParse(SQLParser.ParseContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#sql_stmt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_stmt_list(SQLParser.Sql_stmt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#sql_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_stmt(SQLParser.Sql_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#create_db_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_db_stmt(SQLParser.Create_db_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#drop_db_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_db_stmt(SQLParser.Drop_db_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#create_user_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_user_stmt(SQLParser.Create_user_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#drop_user_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_user_stmt(SQLParser.Drop_user_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#create_table_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_table_stmt(SQLParser.Create_table_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#show_meta_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_meta_stmt(SQLParser.Show_meta_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#grant_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_stmt(SQLParser.Grant_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#revoke_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevoke_stmt(SQLParser.Revoke_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#use_db_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUse_db_stmt(SQLParser.Use_db_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#delete_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDelete_stmt(SQLParser.Delete_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#drop_table_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_table_stmt(SQLParser.Drop_table_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#show_db_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_db_stmt(SQLParser.Show_db_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#quit_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuit_stmt(SQLParser.Quit_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#show_table_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_table_stmt(SQLParser.Show_table_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#insert_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert_stmt(SQLParser.Insert_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#value_entry}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue_entry(SQLParser.Value_entryContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#select_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_stmt(SQLParser.Select_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#create_view_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_view_stmt(SQLParser.Create_view_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#drop_view_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_view_stmt(SQLParser.Drop_view_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#update_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdate_stmt(SQLParser.Update_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#column_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_def(SQLParser.Column_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#type_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_name(SQLParser.Type_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#column_constraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_constraint(SQLParser.Column_constraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#multiple_condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiple_condition(SQLParser.Multiple_conditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCondition(SQLParser.ConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#comparer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparer(SQLParser.ComparerContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#comparator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparator(SQLParser.ComparatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(SQLParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#table_constraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_constraint(SQLParser.Table_constraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#result_column}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResult_column(SQLParser.Result_columnContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#table_query}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_query(SQLParser.Table_queryContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#auth_level}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAuth_level(SQLParser.Auth_levelContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#literal_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral_value(SQLParser.Literal_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#column_full_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_full_name(SQLParser.Column_full_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#database_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatabase_name(SQLParser.Database_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#table_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_name(SQLParser.Table_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#user_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUser_name(SQLParser.User_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#column_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_name(SQLParser.Column_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#view_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitView_name(SQLParser.View_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#password}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPassword(SQLParser.PasswordContext ctx);
}