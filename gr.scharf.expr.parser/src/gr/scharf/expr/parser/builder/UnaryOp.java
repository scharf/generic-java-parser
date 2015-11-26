package gr.scharf.expr.parser.builder;

public enum UnaryOp {
	PLUS("+"),
	MINUS("-"),
	NOT("!"), 
	BIT_COMPLEMENT("~");
	
	private final String fOp;

	private UnaryOp(String op) {
		fOp=op;
	}
	public String getOp() {
		return fOp;
	}
}