package gr.scharf.expr.parser.parser;

import gr.scharf.expr.parser.lexer.ILexer;
import gr.scharf.expr.parser.lexer.IToken;
import gr.scharf.expr.parser.lexer.TokenType;

public class ParserUtil {
	/**
	 * @param lexer
	 * @return the next token that is not whitespace or comment
	 */
	static public IToken nextTokenIgnoringWhitespace(ILexer lexer) {
		IToken token;
		do {
			token = lexer.nextToken();
		} while(token.getType().isWhitespaceOrComment());
		return token;
	}
	/**
	 * @param lexer
	 * @return true if the current token is the end of input
	 */
	public static boolean isEOF(ILexer lexer) {
		return lexer.getCurrentToken().getType().isEOF();
	}
	/**
	 * @param lexer
	 * @param expectedType
	 * @param expectedValue
	 * @throws ParseException when the {@link ILexer#getCurrentToken()} is not the <code>expectedType</code>
	 * and the {@link IToken#getString()} is not <code>expectedValue</code>
	 */
	public static void expect(ILexer lexer,TokenType expectedType, String expectedValue) throws ParseException{
		IToken currentToken = lexer.getCurrentToken();
		if(currentToken.getType()!=expectedType)
			throw new ParseException(currentToken,"expected "+expectedValue);
		if(!currentToken.getString().equals(expectedValue))
			throw new ParseException(currentToken,"expected "+expectedValue);
	}
	/**
	 * @param lexer
	 * @param expectedType
	 * @throws ParseException when the {@link ILexer#getCurrentToken()} is not the <code>expectedType</code>
	 */
	public static void expect(ILexer lexer,TokenType expectedType) throws ParseException{
		IToken currentToken = lexer.getCurrentToken();
		if(currentToken.getType()!=expectedType)
			throw new ParseException(currentToken,"expected "+expectedType);
	}
}
