package gr.scharf.expr.parser.lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import gr.scharf.expr.parser.lexer.impl.ErrorPrinter;
import gr.scharf.expr.parser.lexer.impl.FlexLexer;

public class Lexer extends FlexLexer implements ILexer {

    private IToken          fCurrentToken;
    private boolean         fNotForwardOnNextToken;
    private List<IToken>    fLookAhead;
    private final Exception fException;

    private Lexer(ErrorPrinter errorPrinter) {
        super(errorPrinter);
        fException = new RuntimeException("HERE WAS THE LEXER CERATED");
    }

    /**
     * @param inputString
     */
    public Lexer(String inputString) {
        this(new ErrorPrinter(inputString));
        setInput(new StringReader(inputString));
    }

    /**
     * @param url
     * @throws IOException
     *             if the URL cannot be opened
     */
    public Lexer(URL url) throws IOException {
        this(new ErrorPrinter(url));
        Reader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        setInput(reader);
    }

    public Lexer(URL url, String encoding) throws IOException {
        this(new ErrorPrinter(url));
        Reader reader = new BufferedReader(new InputStreamReader(url.openStream(), encoding));
        setInput(reader);
    }

    public Lexer(Reader reader) {
        this(new ErrorPrinter((URL) null));
        setInput(new BufferedReader(reader));
    }

    protected void setInput(Reader reader) {
        fCurrentToken = null;
        yyreset(reader);
    }

    protected void setInputString(String string) {
        setInput(new StringReader(string));
    }

    @Override
    public IToken nextToken() {
        if (fNotForwardOnNextToken) {
            // we use the current token
            fNotForwardOnNextToken = false;
        } else if (fLookAhead != null && fLookAhead.size() != 0) {
            fCurrentToken = fLookAhead.remove(0);
        } else {
            fCurrentToken = getNotForwardOnNextToken();
        }
        return fCurrentToken;
    }

    private IToken getNotForwardOnNextToken() {
        IToken nextToken;
        try {
            nextToken = super.nextToken();
        } catch (IOException e) {
            nextToken = token(TokenType.UNEXPEXTED_EOF, e.getLocalizedMessage());
        }
        return nextToken;
    }

    @Override
    public IToken getCurrentToken() {
        return fCurrentToken;
    }

    @Override
    public void useCurrentTokenAsNextToken() {
        if (fCurrentToken != null)
            fNotForwardOnNextToken = true;
    }

    @Override
    public IToken lookahead(int n) {
        if (n < 1)
            throw new IllegalArgumentException(n + "<1");
        if (fLookAhead == null) {
            fLookAhead = new ArrayList<IToken>();
        }
        if (fLookAhead.size() < n) {
            for (int i = fLookAhead.size(); i < n; i++) {
                fLookAhead.add(getNotForwardOnNextToken());
            }
        }
        return fLookAhead.get(n - 1);
    }

    @Override
    protected IToken token(TokenType type, String value) {
        IToken token = super.token(type, value);
        token.setSourceException(fException);
        return token;
    }

}
