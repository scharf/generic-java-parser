package gr.scharf.expr.parser.builder;

import static gr.scharf.expr.parser.builder.ExprTokenType.AND;
import static gr.scharf.expr.parser.builder.ExprTokenType.AT;
import static gr.scharf.expr.parser.builder.ExprTokenType.BIT_AND;
import static gr.scharf.expr.parser.builder.ExprTokenType.BIT_COMPLEMENT;
import static gr.scharf.expr.parser.builder.ExprTokenType.BIT_OR;
import static gr.scharf.expr.parser.builder.ExprTokenType.BIT_XOR;
import static gr.scharf.expr.parser.builder.ExprTokenType.COMMA;
import static gr.scharf.expr.parser.builder.ExprTokenType.DIVIDE;
import static gr.scharf.expr.parser.builder.ExprTokenType.DOT;
import static gr.scharf.expr.parser.builder.ExprTokenType.EOF;
import static gr.scharf.expr.parser.builder.ExprTokenType.IDENTIFIER;
import static gr.scharf.expr.parser.builder.ExprTokenType.LSHIFT;
import static gr.scharf.expr.parser.builder.ExprTokenType.MINUS;
import static gr.scharf.expr.parser.builder.ExprTokenType.MODULO;
import static gr.scharf.expr.parser.builder.ExprTokenType.MULT;
import static gr.scharf.expr.parser.builder.ExprTokenType.NOT;
import static gr.scharf.expr.parser.builder.ExprTokenType.NOT_LIKE;
import static gr.scharf.expr.parser.builder.ExprTokenType.NUMBER;
import static gr.scharf.expr.parser.builder.ExprTokenType.OR;
import static gr.scharf.expr.parser.builder.ExprTokenType.PARENCLOSE;
import static gr.scharf.expr.parser.builder.ExprTokenType.PARENOPEN;
import static gr.scharf.expr.parser.builder.ExprTokenType.PLUS;
import static gr.scharf.expr.parser.builder.ExprTokenType.RSHIFT;
import static gr.scharf.expr.parser.builder.ExprTokenType.STRING;
import gr.scharf.expr.parser.lexer.ILexer;
import gr.scharf.expr.parser.lexer.IToken;
import gr.scharf.expr.parser.lexer.Lexer;
import gr.scharf.expr.parser.lexer.TokenType;
import gr.scharf.expr.parser.parser.IParser;
import gr.scharf.expr.parser.parser.ParseException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;


// TODO: re-wirte the parser without the need of this enum....
/*package*/enum ExprTokenType {
	EOF,
	NOT,
	IN,
	LIKE,
	NOT_LIKE,
	IS,
	IS_NOT,
	COMMA,
	AND,
	OR,
	DOT,
	NE,
	LT,
	LE,
	EQ,
	GE,
	GT,
	TRUE,
	FALSE,
	NULL,
	PARENOPEN,
	PARENCLOSE,
	IDENTIFIER,
	NUMBER,
	STRING,
	AT,

	PLUS,
	MINUS,
	MULT,
	DIVIDE,
	MODULO,
	LSHIFT,
	RSHIFT,
	BIT_AND,
	BIT_XOR,
	BIT_OR,
	BIT_COMPLEMENT,
	
	UNEXPECTED_TOKEN;
	
}

public class ExprParser<T,E extends Throwable> implements IParser<T,E> {
	private ILexer fScanner;
	private IToken fToken;
	private ExprTokenType fTokenType;
	private final IExpressionBuilder<T,E> fBuilder;
	private final Collection<String> fKeywords=new HashSet<String>();
	private final IParser<T, E> fUnexpectedTokenParser;
	private final boolean fExpectToEndWithEOF;

	/**
	 * @param builder
	 * @param expectToEndWithEOF if true then an error is thrown if the input does not end with an EOF token.
	 * If <code>false</code> a next parser could parse the next token...
	 */
	public ExprParser(IExpressionBuilder<T,E> builder, boolean expectToEndWithEOF) {
		this(builder,expectToEndWithEOF, null);
	}
	public ExprParser(IExpressionBuilder<T,E> builder, boolean expectToEndWithEOF, IParser<T,E> unexpectedTokenParser) {
		fUnexpectedTokenParser=unexpectedTokenParser;
		fExpectToEndWithEOF=expectToEndWithEOF;
		fBuilder=builder;
		setKeywords("in", "not", "like", "or", "and", "true", "false", "null", "is", "mod");
	}
	public T parseVQL(String snql) throws ParseException, E {
		return parse(new Lexer(snql));
	}
	@Override
	public T parse(ILexer lexer) throws ParseException, E {
		fScanner=lexer;
		fScanner.useCurrentTokenAsNextToken();
		return parseInput();
	}


	private void setKeywords(String... keywords) {
		fKeywords.clear();
		for (String keyword : keywords) {
			fKeywords.add(keyword);
		}		
	}
	/**
	 * Is the given identifier a keyword. Subclasses might decide to do a case-insensitve matching.
	 * @param identifier
	 * @return true if the identifier is considered to be a keyword.
	 */
	private boolean isKeyword(String identifier) {
		return fKeywords.contains(identifier.toLowerCase());
	}
	private T parseInput() throws ParseException, E {
		T node = null;

		nextToken();
		node = parseOrExpression();
		if (fExpectToEndWithEOF && fTokenType != EOF) {
			syntaxError("Expected end of Program "+fToken); //$NON-NLS-1$
		}
		return node;
	}
	private T parseOrExpression() throws ParseException, E {
		T expression = parseAndExpression();
		while (fTokenType == OR) {
			IToken token = fToken;
			nextToken();
			T rightExpression = parseAndExpression();
			expression = fBuilder.buildBinaryOp(token, BinaryOp.OR, expression, rightExpression);
		}
		return expression;
	}

	private T parseAndExpression() throws ParseException, E {
		T expression = parseNotExpression();
		while (fTokenType == AND) {
			IToken token = fToken;
			nextToken();
			T rightExpression = parseAndExpression();
			expression = fBuilder.buildBinaryOp(token, BinaryOp.AND, expression, rightExpression);
		}
		return expression;
	}

	private T parseNotExpression() throws ParseException, E {
		if (fTokenType == NOT) {
			IToken token = fToken;
			nextToken();
			return fBuilder.buildUnaryOp(token, UnaryOp.NOT, parseNotExpression());
		} else {
			return parseCompareExpression();
		}
	}

	private T parseCompareExpression() throws ParseException, E {
		T node = null;
		T left = parseBitOr();
		IToken token = fToken;
		switch (fTokenType) {
		case IN:
			nextToken();
			node = fBuilder.buildFunction(token, null, "in", left, parseBitOr());
			break;
		case LIKE:
		case NOT_LIKE:
			boolean negate = false;
			if (fTokenType == NOT_LIKE) {
				negate = true;
			}
			nextToken();
			// cs is a special Bischi feature: match case sensitive
			// The default is false
			boolean cs = false;
			if (fTokenType == AT) {
				cs = true;
				nextToken();
			}
			if (fTokenType != STRING) {
				node = fBuilder.buildFunction(token, null, "like", left,parseBitOr(), Boolean.valueOf(negate), Boolean.valueOf(cs));
			} else {
				node = fBuilder.buildFunction(token, null, "like", left, getCurrentString(), Boolean.valueOf(negate), Boolean.valueOf(cs));
				nextToken();
			}
			break;
		case IS: {
			nextToken();
			T right = parseBitOr();
			node=fBuilder.buildFunction(token, null, "is", left, right);
			break;
		}
		case IS_NOT: {
			nextToken();
			T right = parseBitOr();
			node=fBuilder.buildFunction(token, null, "is_not", left, right);
			break;
		}
		case NE:
		case LT:
		case LE:
		case EQ:
		case GE:
		case GT: {
			BinaryOp op = currentBinartOp();
			nextToken();
			T right = parseBitOr();
			node = fBuilder.buildBinaryOp(token, op, left, right);
			break;
		}
		default:
			node = left;
			break;
		}
		return node;
	}

	private BinaryOp currentBinartOp() throws ParseException {
		switch (fTokenType) {
		case NE:
			return BinaryOp.NE;
		case LT:
			return BinaryOp.LT;
		case LE:
			return BinaryOp.LE;
		case EQ:
			return BinaryOp.EQ;
		case GE:
			return BinaryOp.GE;
		case GT:
			return BinaryOp.GT;
		case AND:
			return BinaryOp.AND;
		case BIT_AND:
			return BinaryOp.BIT_AND;
		case BIT_OR:
			return BinaryOp.BIT_OR;
		case BIT_XOR:
			return BinaryOp.BIT_XOR;
		case DIVIDE:
			return BinaryOp.DIVIDE;
		case LSHIFT:
			return BinaryOp.L_SHIFT;
		case MINUS:
			return BinaryOp.SUBTRACT;
		case MODULO:
			return BinaryOp.MODULO;
		case MULT:
			return BinaryOp.MULTIPLY;
		case OR:
			return BinaryOp.OR;
		case PLUS:
			return BinaryOp.ADD;
		case RSHIFT:
			return BinaryOp.R_SHIFT;
		default:
			syntaxError("Unexpected binary op "+fTokenType);
			return null;
		}
	}
	
	private T parseBitOr() throws ParseException, E {
		T value = parseBitXOr();
		while (fTokenType == BIT_OR) {
			IToken token = fToken;
			nextToken();
			T rightValue = parseBitXOr();
			value = fBuilder.buildBinaryOp(token, BinaryOp.BIT_OR, value, rightValue);
		}
		return value;
	}

	private T parseBitXOr() throws ParseException, E {
		T value = parseBitAnd();
		while (fTokenType == BIT_XOR) {
			IToken token = fToken;
			nextToken();
			T rightValue = parseBitAnd();
			value = fBuilder.buildBinaryOp(token, BinaryOp.BIT_XOR, value, rightValue);
		}
		return value;
	}

	private T parseBitAnd() throws ParseException, E {
		T value = parseBitShift();
		while (fTokenType == BIT_AND) {
			IToken token = fToken;
			nextToken();
			T rightValue = parseBitShift();
			value = fBuilder.buildBinaryOp(token, BinaryOp.BIT_AND, value, rightValue);
		}
		return value;
	}

	private T parseBitShift() throws ParseException, E {
		T value = parseTerm();
		while (fTokenType == LSHIFT || fTokenType == RSHIFT) {
			IToken token = fToken;
			BinaryOp op = currentBinartOp();
			nextToken();
			T rightValue = parseTerm();
			value = fBuilder.buildBinaryOp(token, op, value, rightValue);
		}
		return value;
	}

	private T parseTerm() throws ParseException, E {
		T value = parseFactor();
		while (fTokenType == PLUS || fTokenType == MINUS) {
			IToken token = fToken;
			BinaryOp op = currentBinartOp();
			nextToken();
			T rightValue = parseFactor();
			value = fBuilder.buildBinaryOp(token, op, value, rightValue);
		}
		return value;
	}

	private T parseFactor() throws ParseException, E {
		T value = parseNegate();
		while (fTokenType == MULT || fTokenType == DIVIDE || fTokenType == MODULO) {
			IToken token = fToken;
			BinaryOp op = currentBinartOp();
			nextToken();
			T rightValue = parseNegate();
			value = fBuilder.buildBinaryOp(token, op, value, rightValue);
		}
		return value;
	}

	private T parseNegate() throws ParseException, E {
		boolean negate = false;
		T value = null;
		IToken token = null;
		while (value == null && (fTokenType == PLUS || fTokenType == MINUS)) {
			switch (fTokenType) {
			case PLUS:
				nextToken();
				break;
			case MINUS:
				negate = !negate;
				token = fToken;
				nextToken();
				// because 1-1 generates 3 tokens we concatenate them here
				// a kind of hack
				if (fTokenType == NUMBER) {
					negate = !negate; // because we do it ;-)
					Object n = getCurrentData();
					if (n instanceof Double)
						value = fBuilder.buildLiteral(fToken, Double.valueOf(-((Double) n).doubleValue()));
					else
						value = fBuilder.buildLiteral(fToken, Long.valueOf(-((Number) n).longValue()));
					nextToken();
				}
				break;
			default:
				syntaxError("FATAL PARSER ERROR"); //$NON-NLS-1$
				break;
			}
		}
		if (value == null) // else it was negative number
			value = parseBitComplement();
		if (negate) {
			value = fBuilder.buildUnaryOp(token, UnaryOp.MINUS, value);
		}
		return value;
	}

	private T parseBitComplement() throws ParseException, E {
		T value;
		if (fTokenType == BIT_COMPLEMENT) {
			IToken token = fToken;
			nextToken();
			value = fBuilder.buildUnaryOp(token, UnaryOp.BIT_COMPLEMENT, parseNegate());
		} else {
			value = parseMember();
		}
		return value;
	}

	private T parseMember() throws ParseException, E {
		T value = parseExpression();
		while (fTokenType == DOT) {
			IToken token = fToken;
			nextToken();
			if (fTokenType != IDENTIFIER) {
				syntaxError("Identifier expected after '.'"); //$NON-NLS-1$
			}
			String name = getCurrentString();
			nextToken();
			if(fTokenType==PARENOPEN) {
				value=parseFunction(token, value, name);
			} else {
				value = fBuilder.buildMemberAccess(token, value, name);
			}
		}
		return value;
	}

	private T parseExpression() throws ParseException, E {
		T node;
		if (fTokenType == PARENOPEN) {
			nextToken();
			node = parseOrExpression();
			if (fTokenType == PARENCLOSE) {
				nextToken();
			} else {
				syntaxError("')' expected"); //$NON-NLS-1$
			}
		} else {
			node = parseValue();
		}
		return node;
	}

	private T parseValue() throws ParseException, E {
		T value = null;
		switch (fTokenType) {
		case TRUE:
		case FALSE:
		case NUMBER:
		case STRING:
			value = fBuilder.buildLiteral(fToken, getCurrentData());
			nextToken();
			break;
		case NULL:
			value = fBuilder.buildLiteral(fToken, null);
			nextToken();
			break;
		case IDENTIFIER: {
			value = parseVariableOrFunction();
			}
			break;
		case UNEXPECTED_TOKEN:
			if(fUnexpectedTokenParser!=null) {
				value=fUnexpectedTokenParser.parse(fScanner);
			} else {
				syntaxError("unexpected token: '"+fToken.getString()+"'"); //$NON-NLS-1$

			}
			break;
		default:
			syntaxError("const value or identifier expected"); //$NON-NLS-1$
			break;

		}
		return value;
	}
	private T parseVariableOrFunction() throws E {
		T value;
		String name = getCurrentString();
		IToken token = fToken;
		nextToken();
		if(fTokenType==PARENOPEN) {
			value=parseFunction(token,null,name);
		} else {
			value = fBuilder.buildVariable(token, name);
		}
		return value;
	}

	private T parseFunction(IToken fNameToken, T self, String name) throws E {
		if(fTokenType!=PARENOPEN) {
			syntaxError("'(' expected"); //$NON-NLS-1$
		}
		nextToken();
		List<Object> parameters=new ArrayList<Object>();
		while(fTokenType!=PARENCLOSE) {
			parameters.add(parseFunctionParameter());
			if(fTokenType==COMMA)
				nextToken();
		}
		if(fTokenType!=PARENCLOSE) {
			syntaxError("')' expected"); //$NON-NLS-1$
		}
		nextToken();
		Object[] array = parameters.toArray();
		return fBuilder.buildFunction(fNameToken, self, name, array);
	}
	
	protected Object parseFunctionParameter() throws ParseException, E {
		if(fTokenType==IDENTIFIER) {
			IToken nextToken = lookaheadNonWhitespace();
			if(nextToken.getType()==TokenType.CHARACTER && nextToken.toChar()==':') {
				String name=fToken.getString();
				nextToken();
				IToken token = fToken;
				nextToken();
				T argument = parseOrExpression();
				return fBuilder.buildNamedArgument(token, name, argument);
			}
		}
		return parseOrExpression();
	}
	private IToken lookaheadNonWhitespace() {
		int n=1;
		IToken lookahead;
		do {
			lookahead=fScanner.lookahead(n++);
		} while(lookahead.getType().isWhitespaceOrComment());
		return lookahead;
	}
	
	private void nextToken() throws ParseException {
		do {
			fToken = fScanner.nextToken();
			// skip the whitespace
		} while(fToken.getType().isWhitespaceOrComment());
		fTokenType = toType();
	}

	private ExprTokenType toType() throws ParseException {
		TokenType type = fToken.getType();
		switch (type) {
		case AND:
			return ExprTokenType.AND;
		case CHARACTER:
			switch (fToken.toChar()) {
			case '<':
				return ExprTokenType.LT;
			case '=':
				return ExprTokenType.EQ;
			case '>':
				return ExprTokenType.GT;
			case '+':
				return ExprTokenType.PLUS;
			case '-':
				return ExprTokenType.MINUS;
			case '*':
				return ExprTokenType.MULT;
			case '/':
				return ExprTokenType.DIVIDE;
			case '%':
				return ExprTokenType.MODULO;
			case '&':
				return ExprTokenType.BIT_AND;
			case '^':
				return ExprTokenType.BIT_XOR;
			case '|':
				return ExprTokenType.BIT_OR;
			case '~':
				return ExprTokenType.BIT_COMPLEMENT;
			case '(':
				return ExprTokenType.PARENOPEN;
			case ')':
				return ExprTokenType.PARENCLOSE;
			case '.':
				return ExprTokenType.DOT;
			case ',':
				return ExprTokenType.COMMA;
			case '@':
				return ExprTokenType.AT;
			case '!':
				return ExprTokenType.NOT;
			default:
				return ExprTokenType.UNEXPECTED_TOKEN;
			}
		case DQSTRING:
		case SQSTRING:
			return ExprTokenType.STRING;
		case EOF:
			return ExprTokenType.EOF;
		case EQ:
			return ExprTokenType.EQ;
		case FLOAT:
			return ExprTokenType.NUMBER;
		case GE:
			return ExprTokenType.GE;
		case INTEGER:
			return ExprTokenType.NUMBER;
		case IDENTIFIER: {
			if(!isKeyword(fToken.getString())) {
				if(fToken.getString().equals("%"))
					return ExprTokenType.MODULO;
				return ExprTokenType.IDENTIFIER;
			} else {
				String s = fToken.getString().toLowerCase();
				// "in","not","like","or","and","true","false","null","is"
				if (s.equals("in"))
					return ExprTokenType.IN;
				if (s.equals("not")) {
					if (isNextToken("like")) {
						nextToken();
						return ExprTokenType.NOT_LIKE;
					}
					return ExprTokenType.NOT;
				}
				if (s.equals("like"))
					return ExprTokenType.LIKE;
				if (s.equals("or"))
					return ExprTokenType.OR;
				if (s.equals("and"))
					return ExprTokenType.AND;
				if (s.equals("true"))
					return ExprTokenType.TRUE;
				if (s.equals("false"))
					return ExprTokenType.FALSE;
				if (s.equals("null"))
					return ExprTokenType.NULL;
				if (s.equals("mod"))
					return ExprTokenType.MODULO;
				if (s.equals("is")) {
					if (isNextToken("not")) {
						nextToken();
						return ExprTokenType.IS_NOT;
					}
					return ExprTokenType.IS;
				}
				syntaxError("Unexpected keyword " + fToken.getString());
				return null;
			}

		}
		case LE:
			return ExprTokenType.LE;
		case LSHIFT:
			return ExprTokenType.LSHIFT;
		case NE:
			return ExprTokenType.NE;
		case OR:
			return ExprTokenType.OR;
		case RSHIFT:
			return ExprTokenType.RSHIFT;
		case COMMENT:
		case WHITESPACE:
			syntaxError("Unexpected token " + fToken.getString());
			return null;
		case UNEXPEXTED_EOF:
			syntaxError("Unexpected end of file " + fToken.getString());
			return null;
		}
		syntaxError("Unexpected token " + fToken.getString());
		return null;
	}

	private boolean isNextToken(String keyword) {
		IToken t;
		do {
			t = fScanner.nextToken();
		} while (t.getType().isWhitespaceOrComment());
		fScanner.useCurrentTokenAsNextToken();
		if (t.getType() == TokenType.IDENTIFIER && t.getString().toLowerCase().equals(keyword))
			return true;
		return false;
	}

	private String getCurrentString() {
		return fToken.getString(); 
	}

	private Object getCurrentData() {

		TokenType type = fToken.getType();
		if (type.isFloat())
			return Double.valueOf(fToken.toDouble());
		if (type.isInteger())
			return Long.valueOf(fToken.toLong());
		switch (fTokenType) {
		case TRUE:
			return Boolean.TRUE;
		case FALSE:
			return Boolean.FALSE;
		case NULL:
			return null;
		default:
			return fToken.getString();
		}
	}

	private void syntaxError(String msg) throws ParseException {
		throw new ParseException(fToken,msg);
	}

}
