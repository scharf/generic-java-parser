package gr.scharf.expr.parser.lexer;

public interface IToken {
    TokenType getType();

    /**
     * @return the string value of the token. <code>null</code> only for EOF
     */
    String getString();

    /**
     * @return line number starting with line 1
     */
    int getLineNumber();

    /**
     * @return column number starting with column 1
     */
    int getColumnNumber();

    /**
     * @param message
     * @return a nicely formatted string that shows the message in the context
     *         of the last lines.
     */
    String getFormattedErrorMessage(String message);

    /**
     * @return the current value as long.
     * @exception NumberFormatException
     *                if the {@link #getString()} does not contain a parsable
     *                <code>long</code>.
     */
    long toLong();

    /**
     * @return the current value as double.
     * @exception NumberFormatException
     *                if the {@link #getString()} does not contain a parsable
     *                <code>double</code>.
     */
    double toDouble();

    /**
     * @return the first char of the string value
     */
    char toChar();

    /**
     * @param type
     *            the type of the token
     * @param value
     *            the string of that token
     * @return a new token using the this token position
     */
    IToken createToken(TokenType type, String value);

    Exception getSourceException();

    void setSourceException(Exception ex);

}
