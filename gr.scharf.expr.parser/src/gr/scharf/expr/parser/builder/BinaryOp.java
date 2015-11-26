package gr.scharf.expr.parser.builder;

public enum BinaryOp {
	AND("&&"),
	OR("||"),
	NE("!="),
	LT("<"),
	LE("<="),
	EQ("=="),
	GE(">="),
	GT(">"),
	ADD("+"),
	SUBTRACT("-"),
	MULTIPLY("*"),
	DIVIDE("/"),
	MODULO("%"),
	BIT_AND("&"),
	BIT_OR("|"),
	BIT_XOR("^"),
	L_SHIFT("<<"),
	R_SHIFT(">>");
	
	private final String fOp;

	private BinaryOp(String op) {
		fOp=op;
	}
	public String getOp() {
		return fOp;
	}
	
	public boolean isBoopeanOp() {
		switch (this) {
		case ADD:
			return false;
		case AND:
			return true;
		case BIT_AND:
			return false;
		case BIT_OR:
			return false;
		case BIT_XOR:
			return false;
		case DIVIDE:
			return false;
		case EQ:
			return true;
		case GE:
			return true;
		case GT:
			return true;
		case LE:
			return true;
		case LT:
			return true;
		case L_SHIFT:
			return false;
		case MODULO:
			return false;
		case MULTIPLY:
			return false;
		case NE:
			return true;
		case OR:
			return true;
		case R_SHIFT:
			return false;
		case SUBTRACT:
			return false;
		}
		throw new IllegalStateException("OP="+this);
	}
}