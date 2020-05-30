package cn.edu.thssdb.parser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * 描述:详细的error listener，能告诉用户具体出问题的位置
 */
public class MyErrorListener extends BaseErrorListener {
	static final MyErrorListener instance = new MyErrorListener();
	
	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offending_symbol, int line, int position_in_line,
	                        String message, RecognitionException exception) {
		throw new ParseCancellationException("line " + line + ":" + position_in_line + " " + message);
	}
}

