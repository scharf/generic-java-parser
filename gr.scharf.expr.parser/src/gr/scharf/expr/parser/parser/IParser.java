package gr.scharf.expr.parser.parser;

import gr.scharf.expr.parser.lexer.ILexer;
import gr.scharf.expr.parser.lexer.TokenType;

public interface IParser<T, E extends Throwable> {
	/**
	 * Parser expects the {@link ILexer#getCurrentToken()} to be at the first token.
	 * On exit it will be at the next token (the first one not consumed by the parser,
	 * may be {@link TokenType#EOF}.
	 * @param lexer
	 * @return
	 * @throws ParseException
	 */
	T parse(ILexer lexer) throws ParseException, E;
}
