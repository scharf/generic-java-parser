package gr.scharf.expr.parser.lexer.impl;

import gr.scharf.expr.parser.lexer.IToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

public class ErrorPrinter {
	private String fInputString;
	private URL fURL;
	
	public ErrorPrinter(String inputString) {
		fInputString = inputString;
	}

	public ErrorPrinter(URL url) {
		fURL=url;
	}

	public String toErrorString(IToken token, String msg, int maxLinesToReport) {
		int n = token.getLineNumber();
		int firstLineToPrint=Math.max(1, n-maxLinesToReport);
		int iLine=1;
		StringBuilder result=new StringBuilder();
		if(fURL!=null) {
			result.append(fURL);
			result.append(": ");
		}
		result.append("line=");
		result.append(token.getLineNumber());
		result.append(" column=");
		result.append(token.getColumnNumber());
		result.append("\n");
		String lineWithError="";
		try {
			String line;
			BufferedReader reader=makeReader();
			if (reader != null) {
				while((line=reader.readLine())!=null && iLine>=firstLineToPrint && iLine<=n) {
					result.append(line);
					result.append("\n");
					lineWithError=line;
					iLine++;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// to get the ^ at the correct position, we take the line with errors and
		// replace all non tab characters with a space. Then the ^ will always
		// be shown at the same position independent of the with of tab in the
		// display....
		if (lineWithError.length() > 0) {
			lineWithError=lineWithError.replaceAll("[^\t ]", " ");
			result.append(lineWithError.substring(0,Math.min(token.getColumnNumber()-1,lineWithError.length())));
			result.append("^ ");
		}
		result.append(msg);
		return result.toString();
	}

	private BufferedReader makeReader() throws IOException {
		if(fURL!=null)
			return new BufferedReader(new InputStreamReader(fURL.openStream()));
		if (fInputString != null)
			return new BufferedReader(new StringReader(fInputString));
		return null;
	}
	
}
