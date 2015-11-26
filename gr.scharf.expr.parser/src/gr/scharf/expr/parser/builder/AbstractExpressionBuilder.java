package gr.scharf.expr.parser.builder;

import gr.scharf.expr.parser.lexer.IToken;
import gr.scharf.expr.parser.parser.ParseException;

public abstract class AbstractExpressionBuilder<T, E extends Throwable> implements IExpressionBuilder<T, E> {

	@Override
	public T buildBinaryOp(IToken token, BinaryOp op, T left, T right) throws E {
		return buildFunction(token, null, opToFunction(token.getString()), left, right);
	}
	protected String opToFunction(String op) {
		return "__"+op+"__";
	}
	@Override
	public T buildUnaryOp(IToken token, UnaryOp op, T self) throws E {
		return buildFunction(token, null, opToFunction(token.getString()), self);
	}

	@Override
	public T buildMemberAccess(IToken token, T self, String member) throws E {
		return buildFunction(token, null, "__MEMBER",self, buildLiteral(token, member));
	}

	@Override
	public Object buildNamedArgument(IToken token, String name, T argument) throws E {
		throw new ParseException(token, "Named arguments are not supported " + name);
	};
	
}
