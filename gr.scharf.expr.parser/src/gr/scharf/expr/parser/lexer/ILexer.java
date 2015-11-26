package gr.scharf.expr.parser.lexer;


public interface ILexer {
	/**
	 * Moves to the next token.  End of file is handled by the {@link TokenType#EOF}. If the file end is
	 * reached pre-maturely (like within a string) the {@link TokenType#UNEXPEXTED_EOF} token is returned.
	 * In that case you can get to the error message with {@link IToken#getString()}.
	 * @return never returns <code>null</code>.
	 */
	IToken nextToken();
	/**
	 * @return the current token or <code>null</code> {@link #nextToken()} has not been called.
	 */
	IToken getCurrentToken();

	/**
	 * @param n n must be n>=1
	 * @return the nth token form current without moving the current token forward
	 */
	IToken lookahead(int n);
	
	/**
	 * The next call to {@link #nextToken()} will return the {@link #getCurrentToken()}.
	 * If the {@link #getCurrentToken()} is <code>null</code> this call has no effect.
	 * <p>
	 * This is very useful, when handing the this object to another parser.
	 * Your parser can safely call this method before it starts parsing. 
	 * <OL>
	 * <LI> If this is a new lexer, then the first token of the stream is returned on {@link #nextToken()}.
	 * <LI> If another parser hands a lexer to you, then the current token is treated as the token that
	 * {@link #nextToken()} returns.
	 * </UL>
	 */
	void useCurrentTokenAsNextToken();
}
