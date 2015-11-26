package gr.scharf.expr.parser.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import gr.scharf.expr.parser.builder.ExprParser;
import gr.scharf.expr.parser.lexer.Lexer;

public class TestExprParser {
    private String parse(String expr) {
        return new ExprParser<String, RuntimeException>(new TestExprBuilder(), true).parse(new Lexer(expr));
    }

    @Test
    public void testAnd() {
        assertEquals("((true && false) || true)", parse("true and false or true"));
    }

    @Test
    public void testFunction() {
        assertEquals("f()", parse("f()"));
        assertEquals("f%unc()", parse("f%unc()"));
        assertEquals("foo((7 - 9), 0.034)", parse("foo(7-9,3.4E-2)"));
        assertEquals("foo(3)", parse("foo ( 3 )"));
    }

    @Test
    public void testMFunction() {
        assertEquals("x.method()", parse("x.method()"));
        assertEquals("x.method(x)", parse("x.method(x)"));
        assertEquals("x.method(x, 3)", parse("x   . /*aga*/  \nmethod(\nx,\n3)"));
        assertEquals("x.method().bar(1, 1, 3, (7 - 9), 0.034)", parse("x.method().bar(1,1,3,7-9,3.4E-2)"));
        assertEquals("x.method().bar(x.foo(y.x, bar(3)))", parse("x.method().bar(x.foo(y.x,bar(3)))"));
    }

    @Test
    public void testExpr() {
        assertEquals("((((1 * 2) + 1) & 4) | (6 ^ (((7 * 8) >> x.r) << f(1.3, \"aa\").g().x)))",
            parse("1*2+1&4|6^7*8>>x.r<<f(1.3,'aa').g().x"));
    }

    @Test
    public void testPseudoFunctions() {
        assertEquals("(x % 10)", parse("x % 10"));
        assertEquals("(x % 10)", parse("x mod 10"));
    }

    @Test
    public void testPseudoFunctions2() {
        assertEquals("(x || 10)", parse("x or 10"));
        assertEquals("((a || (b && c)) || d)", parse("a or b and c or d"));
    }

    @Test
    public void testFunctionWithNamedArgs() {
        assertEquals("f(x:(1 + 2), bar:(1 && 2))", parse("f ( x : 1+2, bar: 1 && 2) "));
    }
}
