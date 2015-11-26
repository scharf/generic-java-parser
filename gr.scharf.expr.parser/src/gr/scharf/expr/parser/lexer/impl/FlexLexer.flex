package gr.scharf.expr.parser.lexer.impl;
import java.io.IOException;
import gr.scharf.expr.parser.lexer.TokenType;
import gr.scharf.expr.parser.lexer.IToken;

%%

/* set the class name */
%public %class FlexLexer

/* use Latin1 characters */
%unicode

/* ignore case in keywords */
%ignorecase

/* creates DFA with switch-statement */
/* higher performance, but larger file */
%switch

/* char counting from begin of input */
%char

/* line counting from begin of input*/
%line

/* char counting from begin of line */
%column

%type IToken

/* own part */
%{
	/* string for hold a part of the result */
	private String fMatchedText;
	private ErrorPrinter fErrorPrinter;
	/**
	 * <P>
	 * standard constructor.
	 * </P>
	 */

	public FlexLexer(ErrorPrinter errorPrinter) {
		fMatchedText = "";
		fErrorPrinter=errorPrinter;
		
	}

	protected void handleComment() {
	}

	protected void handleDocComment() {
	}

	protected void handleCommentBlock() {
	}

	private IToken token(TokenType type) {
		return token(type,null);
	}

	/**
	 * <P>
	 * Creates a new Token for parser.
	 * </P>
	 * 
	 * @param: <B>type</B> token type from TokenType.
	 * @param: <B>Object</B> for token with a classvalue.
	 * @return: <B>Token</B> new token for parser.
	 */

	protected IToken token(TokenType type, String value) {
		return new Token(type, yyline, yycolumn, value, fErrorPrinter);
	}

	/** return the next token token */
	public IToken nextToken() throws java.io.IOException {
		return yylex();
	}

	private String decodeUnicode(String quoted) {
		char c=(char) Integer.parseInt(quoted.substring(2), 16);	
		return ""+c;
	}


	protected boolean dummy() {
		// dummy method to get rid of compiler warnings for unused variables
		return yychar == 0 || zzAtBOL || zzEOFDone;
	}

%}

/* execute, when the end of file is reached */
%eofval{
	return token( TokenType.EOF );
%eofval}

/* copied into the constructors */
%init{
		fMatchedText = "";
%init}

/* special character */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
Whitespace = [ \t\f\r\n]+

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment} | {DocumentationComment} 

TraditionalComment   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment     = "//" {InputCharacter}* {LineTerminator}
DocumentationComment = "/**" {CommentContent} "*"+ "/"
CommentContent       = ( [^*] | \*+ [^/*] )*

Identifier = [a-zA-Z_%][a-zA-Z0-9_%@]*
/* for strings "" */
%state STRING
%state SQSTRING

Zero = 0
DecInt = [1-9][0-9]*
OctalInt = 0[0-7]+
HexInt = 0[xX][0-9a-fA-F]+
Integer = ( {Zero} | {DecInt} | {OctalInt} | {HexInt} )[lL]?
Exponent = [eE] [\+\-]? [0-9]+
Float1 = [0-9]+ \. [0-9]+ {Exponent}?
Float2 = \. [0-9]+ {Exponent}?
Float3 = [0-9]+ \. {Exponent}?
Float4 = [0-9]+ {Exponent}
Float = ( {Float1} | {Float2} | {Float3} | {Float4} ) [fFdD]? |
[0-9]+ [fFDd]
UnicodeChar =\\u[0-9a-f][0-9a-f][0-9a-f][0-9a-f]

%%

/* token */
<YYINITIAL> {
	{Integer}	{ return token(TokenType.INTEGER,  yytext()); }
	{Float}			{ return token(TokenType.FLOAT,  yytext()); }
	{Identifier}	{ return token(TokenType.IDENTIFIER,  yytext()); }
	"&&"	{ return token(TokenType.AND, yytext()); }
	"||"	{ return token(TokenType.OR, yytext()); }
	"!="	{ return token(TokenType.NE, yytext()); }
	">="	{ return token(TokenType.GE, yytext()); }
	"<="	{ return token(TokenType.LE, yytext()); }
	"=="	{ return token(TokenType.EQ, yytext()); }
	"<<"	{ return token(TokenType.LSHIFT, yytext()); }
	">>"	{ return token(TokenType.RSHIFT, yytext()); }
	\"		{ fMatchedText = new String(); yybegin(STRING); }	
	\'		{ fMatchedText = new String(); yybegin(SQSTRING); }	
	{Whitespace}   { return token(TokenType.WHITESPACE,  yytext());} 
	{Comment}      { return token(TokenType.COMMENT,  yytext());} 
	.   	{ return token(TokenType.CHARACTER, yytext()); }
	<<EOF>>		{ return token(TokenType.EOF); }
}
/* end of file reached */

/* strings */
<STRING> {
	\"			{ yybegin(YYINITIAL);
				  return token(TokenType.DQSTRING, fMatchedText); }
	{UnicodeChar} { fMatchedText += decodeUnicode(yytext()); }
	/* cast backslash */
	\\\\		{ fMatchedText += "\\"; }
	/* cast " */
	\\\"		{ fMatchedText += "\""; }
	/* special characters */
	\\t			{ fMatchedText += "\t"; }
	\t			{ fMatchedText += yytext(); }
	\\n			{ fMatchedText += "\n"; }
	\\r			{ fMatchedText += "\r"; }
	\\			{ fMatchedText += yytext(); }
	\r|\n|\r\n	{ throw new IOException("Unterminated string constant: "+ fMatchedText); }
	[^\r\n\t\"\\]+			{  fMatchedText += yytext(); }
 
	<<EOF>>		{ throw new IOException("EOF in unterminated string constant: "+ fMatchedText); }
}
<SQSTRING> {
	\'			{ yybegin(YYINITIAL);
				  return token(TokenType.SQSTRING, fMatchedText); }
	{UnicodeChar} { fMatchedText += decodeUnicode(yytext()); }
	/* cast backslash */
	\\\\		{ fMatchedText += "\\"; }
	/* cast " */
	\\\'		{ fMatchedText += "'"; }
	/* special characters */
	\\t			{ fMatchedText += "\t"; }
	\t			{ fMatchedText += yytext(); }
	\\n			{ fMatchedText += "\n"; }
	\\r			{ fMatchedText += "\r"; }
	\\			{ fMatchedText += yytext(); }
	\r|\n|\r\n	{ throw new IOException("Unterminated string constant: "+ fMatchedText); }
	[^\r\n\t\'\\]+			{  fMatchedText += yytext(); }
 
	<<EOF>>		{ throw new IOException("EOF in unterminated string constant: "+ fMatchedText); }
}

