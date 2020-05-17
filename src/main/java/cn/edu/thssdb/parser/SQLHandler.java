package cn.edu.thssdb.parser;

import cn.edu.thssdb.schema.Manager;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * 描述:处理sql的主类，需要传入manager
 */
public class SQLHandler{
	private Manager manager;
	
	public SQLHandler(Manager manager) {
		this.manager = manager;
	}
	
	public String evaluate(String statement) {
		//词法分析
		SQLLexer lexer = new SQLLexer(CharStreams.fromString(statement));
		lexer.removeErrorListeners();
		lexer.addErrorListener(MyErrorListener.instance);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		
		//句法分析
		SQLParser parser = new SQLParser(tokens);
		parser.removeErrorListeners();
		parser.addErrorListener(MyErrorListener.instance);
		
		//语义分析
		try {
			MyVisitor visitor = new MyVisitor(manager);
			return String.valueOf(visitor.visitParse(parser.parse()));
		} catch (Exception e) {
			return "Exception: illegal SQL statement! Error message: " + e.getMessage();
		}
	}
}
