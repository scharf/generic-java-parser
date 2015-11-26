package gr.scharf.expr.parser.lexer.impl;

import gr.scharf.expr.parser.lexer.IToken;
import gr.scharf.expr.parser.lexer.TokenType;

class Token implements IToken {
    private final TokenType    fTokenType;
    private final int          fLine;
    private final int          fColumn;
    private final String       fValue;
    private Exception          fException;
    private final ErrorPrinter fErrorPrinter;

    public Token(TokenType type, int line, int column, String data, ErrorPrinter errorPrinter) {
        fTokenType = type;
        fLine = line + 1;
        fColumn = column + 1;
        fValue = data;
        fErrorPrinter = errorPrinter;
    }

    @Override
    public int getLineNumber() {
        return fLine;
    }

    @Override
    public int getColumnNumber() {
        return fColumn;
    }

    @Override
    public TokenType getType() {
        return fTokenType;
    }

    @Override
    public String getString() {
        return fValue;
    }

    @Override
    public String toString() {
        return "type=" + fTokenType + " line=" + fLine + " column=" + fColumn + " text='" + fValue + "'";
    }

    @Override
    public long toLong() {
        return Long.decode(getString()).longValue();
    }

    @Override
    public double toDouble() {
        return Double.parseDouble(getString());
    }

    @Override
    public char toChar() {
        return fValue.charAt(0);
    }

    @Override
    public String getFormattedErrorMessage(String message) {
        return fErrorPrinter.toErrorString(this, message, 5);
    }

    @Override
    public IToken createToken(TokenType type, String value) {
        return new Token(type, fLine, fColumn, value, fErrorPrinter);
    }

    @Override
    public Exception getSourceException() {
        return fException;
    }

    @Override
    public void setSourceException(Exception ex) {
        fException = ex;

    }
}
