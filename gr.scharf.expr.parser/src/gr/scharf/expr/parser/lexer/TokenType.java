package gr.scharf.expr.parser.lexer;

public enum TokenType {
	/**
	 * The token matches an identifier
	 */
	IDENTIFIER, 
	/**
	 * An identifier that has been declared keyword by {@link ILexer#setKeywords(String...)}
	 */
	INTEGER,
	/**
	 * floating point number
	 */
	FLOAT,
	/**
	 * This was a single quoted string
	 */
	SQSTRING,
	/**
	 * This was a double quotes string
	 */
	DQSTRING,
	/**
	 * spaces, tabs and newlines
	 */
	WHITESPACE,
	/**
	 * Comment including the comment characters. Supports
	 * C style comments as well as C++ one line comments.
	 */
	COMMENT,
	/**
	 * &&
	 */
	AND,
	/**
	 * ||
	 */
	OR,
	/**
	 * !=
	 */
	NE,
	/**
	 * <=
	 */
	LE,
	/**
	 * ==
	 */
	EQ,
	/**
	 * >=
	 */
	GE,
	/**
	 * <<
	 */
	LSHIFT,
	/**
	 * >>
	 */
	RSHIFT,
	/**
	 * Any other character
	 */
	CHARACTER,
	/**
	 * When the end of file/input stream has reached
	 */
	EOF,
	/**
	 * When the end of file/input stream has reached prematurely (like within a unterminated string)
	 */
	UNEXPEXTED_EOF;

	public boolean isString() {
		return this==SQSTRING || this==DQSTRING;
	}
	public boolean isNumber() {
		return this==INTEGER || this==FLOAT;
	}
	public boolean isInteger() {
		return this==INTEGER;
	}
	public boolean isFloat() {
		return this==FLOAT;
	}
	public boolean isIdentifier() {
		return this==IDENTIFIER;
	}
	public boolean isCharacter(){
		return this==CHARACTER;
	}
	public boolean isCharacters() {
		switch (this) {
		case AND:
		case OR:
		case NE:
		case LE:
		case EQ:
		case GE:
		case LSHIFT:
		case RSHIFT:
		case CHARACTER:
			return true;
		default:
			return false;
		}
	}
	public boolean isEOF() {
		return this==EOF;
	}
	public boolean isEnd() {
		switch (this) {
		case EOF:
		case UNEXPEXTED_EOF:
			return true;
		default:
			return false;
		}
	}
	public boolean isWhitespaceOrComment() {
		switch (this) {
		case WHITESPACE:
		case COMMENT:
			return true;
		default:
			return false;
		}
	}
	public boolean isWhitespace() {
		return this==WHITESPACE;
	}
	public boolean isComment() {
		return this==COMMENT;
	}
}