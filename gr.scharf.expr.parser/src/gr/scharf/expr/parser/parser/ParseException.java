package gr.scharf.expr.parser.parser;

import gr.scharf.expr.parser.lexer.IToken;

public class ParseException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final IToken fToken;
	
	public ParseException(IToken token, String msg) {
		super(token.getFormattedErrorMessage(msg));
		fToken=token;
	}
	public IToken getToken() {
		return fToken;
	}
}
