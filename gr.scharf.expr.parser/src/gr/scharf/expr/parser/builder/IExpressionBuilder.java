package gr.scharf.expr.parser.builder;

import gr.scharf.expr.parser.lexer.IToken;
import gr.scharf.expr.parser.parser.ParseException;

/**
 * @param <T> used by the expression parser to build a parse tree.
 * <p>It is up to the class to allow or deny use of <code>null</code>
 * for <T>.
 * <p> Any method can throw a {@link ParseException}
 */
public interface IExpressionBuilder<T, E extends Throwable> {
	/**
	 * @param token used for error reporting
	 * @param value can be {@link Boolean}, {@link Number},
	 * {@link String} or <code>null</code>.
	 * @return a representation of a literal value
	 * @throws ParseException
	 */
	T buildLiteral(IToken token, Object value) throws E;
	/**
	 * @param token
	 * @param op
	 * @param left
	 * @param right
	 * @return a representation of the binary opetation
	 * @throws ParseException
	 */
	T buildBinaryOp(IToken token, BinaryOp op, T left, T right) throws E;
	/**
	 * @param token used for error reporting
	 * @param op
	 * @param self
	 * @return
	 * @throws ParseException
	 */
	T buildUnaryOp(IToken token, UnaryOp op, T self) throws E;
	/**
	 * @param token
	 * @param self 
	 * @param member name of the member
	 * @return
	 * @throws ParseException
	 */
	T buildMemberAccess(IToken token, T self, String member) throws E;
	/**
	 * @param token used for error reporting
	 * @param self may be <code>null</code>
	 * @param function name of the function
	 * @param args arguments depending on the function. Elements can be <code>T</code>, {@link Boolean}, {@link Number},
	 * {@link String} or <code>null</code>.
	 * @return a node representing the function
	 * @throws ParseException
	 */
	T buildFunction(IToken token, T self, String function, Object...args) throws E;
	
	/**
	 * Used to support named arguments to functions: foo(name:1,bar:17+4)
	 * @param token
	 * @param name
	 * @param argument
	 * @return an object that represents the named argument
	 */
	Object buildNamedArgument(IToken token, String name, T argument) throws E;
	/**
	 * @param token
	 * @param name
	 * @return an object representing the (global) variable 
	 */
	T buildVariable(IToken token, String name) throws E;
}
