package gr.scharf.expr.parser.tests;

import gr.scharf.expr.parser.builder.BinaryOp;
import gr.scharf.expr.parser.builder.IExpressionBuilder;
import gr.scharf.expr.parser.builder.UnaryOp;
import gr.scharf.expr.parser.lexer.IToken;


public class TestExprBuilder implements IExpressionBuilder<String, RuntimeException>{

	@Override
	public String buildLiteral(IToken token, Object value) {
		if(value==null)
			return "NULL";
		if(value instanceof String)
			return "\"" + value + "\"";
		return value.toString();
	}

	@Override
	public String buildBinaryOp(IToken token, BinaryOp op, String left, String right) {
		return "("+left+" "+toString(op)+" "+right+")";
	}

	private String toString(BinaryOp op) {
		return op.getOp();
	}

	private String toString(UnaryOp op) {
		return op.getOp();
	}

	@Override
	public String buildUnaryOp(IToken token,UnaryOp op,
			String self) {
		return toString(op)+" "+self;
	}

	@Override
	public String buildMemberAccess(IToken token, String self, String member) {
		return self+"."+member;
	}

	@Override
	public String buildFunction(IToken token, String self, String function, Object... args) {
		StringBuilder b=new StringBuilder();
		if(self!=null) {
			b.append(self);
			b.append(".");
		}
		b.append(function);
		b.append("(");
		for (int i = 0; i < args.length; i++) {
			if(i>0)
				b.append(", ");
			b.append(args[i]);
		}
		b.append(")");
		return b.toString();
	}

	@Override
	public String buildVariable(IToken token, String name) {
		return name;
	}

	@Override
	public Object buildNamedArgument(IToken token, String name, String argument) {
		return name+":"+argument;
	}

}
