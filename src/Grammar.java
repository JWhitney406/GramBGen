import java.io.File;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Grammar {
    private HashMap<String, Decision> decisions = new HashMap<String, Decision>();
    
    public Grammar(File file) throws FileNotFoundException {
	Scanner fileScanner = new Scanner(file);
	StringBuffer fileBuffer = new StringBuffer();

	while(fileScanner.hasNext()) {
	    fileBuffer.append(fileScanner.nextLine());
	    fileBuffer.append("\n");
	}
	fileBuffer.append("\u0000"); //add a null terminator
	fileScanner.close();
	
	char[] rawFile = fileBuffer.toString().toCharArray();
	Parser fileParser = new Parser(rawFile);

	Decision lastDecision = null;
	while((lastDecision = fileParser.nextDecision()) != null) {
	    decisions.put(lastDecision.getName(), lastDecision);
	}
    }

    public Decision getDecision(String name) {
	return decisions.get(name);
    }

    public static void main(String[] args) throws FileNotFoundException {
	if(args.length != 1) {
	    System.out.println("Usage: <grammar file>");
	    System.exit(1);
	}

	Grammar g = new Grammar(new File(args[0]));
	
	Scanner testScanner = new Scanner(System.in);
	String query = null;
	while(!(query = testScanner.nextLine()).equals("quit")) {
	    Decision res = g.decisions.get(query);

	    System.out.println(res);
	}
	testScanner.close();
    }

    private class Parser {
	private char[] buf;
	private int pos = -1;
	private char ch;

	public Parser(char[] rawFile) {
	    buf = rawFile;
	    scanChar();
	}

	public Decision nextDecision() {
	    //skip whitespace first
	    skipWhitespace();
	    
	    if(isEOF())
		return null;

	    String name = scanString();

	    //allow whitespace between decision name and body
	    skipWhitespace();
	    boolean appendDecision = ch == '+';

	    if(ch == '+')
		scanChar();
	    
	    if(ch != '{') {
		System.out.println("Parse Error: Expected start of block '{', found '"+ch+"'.");
		System.out.println("Name read: "+name);
		System.exit(1);
	    }
	    scanChar();

	    //try to find the paths now
	    skipWhitespace();
	    
	    List<Path> paths = new ArrayList<Path>();
	    
	    while(ch != '}') {
		paths.add(scanPath());
		skipWhitespace();
	    }
	    scanChar(); //get off the '}' character
	    Path[] pathArr = paths.toArray(new Path[paths.size()]);
	    
	    return new Decision(name, pathArr, appendDecision);
	}

	private void scanChar() {
	    ch = buf[++pos];
	}

	private void skipWhitespace() {
	    while(isWhitespace()) {
		scanChar();
	    }
	}

	private boolean isEOF() {
	    return ch == '\u0000';
	}
	
	private boolean isEOL() {
	    return ch == '\n';
	}

	private boolean isWhitespace() {
	    return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r' || ch == '\f';
	}

	private String scanString() {
	    StringBuffer resBuffer = new StringBuffer();
	    while(!isWhitespace() && !isEOF()) {
		resBuffer.append(ch);
		scanChar();
	    }
	    return resBuffer.toString();
	}

	private Path scanPath() {
	    StringBuffer resBuffer = new StringBuffer();
	    List<Use> uses = new ArrayList<Use>();
	    String signal = null;
	    boolean escape = false;


	    while(escape || (!isEOL() && !isEOF() && ch != '}')) {
		if(escape) {
		    if(isEOL()) {
			skipWhitespace();
			escape = false;
		    }  else {
			if(isEOF()) {
			    System.out.println("Unexpected EOF while parsing path");
			    System.exit(1);
			}
			if(ch == 'n')
			    resBuffer.append('\n');
			else 
			    resBuffer.append(ch);
			scanChar();
			escape = false;
		    }
		} else if(ch == '[') { //need to scan a use
		    uses.add(scanUse());
		} else if(ch == '\\') { //check for an excape
		    escape = true;
		    scanChar();
		} else { //if we didn't just scan a use or an escape, then we can add to the signal
		    resBuffer.append(ch);
		    scanChar();
		}
	    }

	    if(isEOF()) {
		System.out.println("Unexpected EOF while parsing path");
		System.exit(1);
	    }
	
	    Use[] useArr = uses.toArray(new Use[uses.size()]);
	    return new Path(resBuffer.toString(), useArr);
	}

	private Use scanUse() {
	    scanChar(); //skip the starting '['
	    skipWhitespace(); //skip any whitespace at the start
	    
	    StringBuffer resBuffer = new StringBuffer();
	    boolean reuse = false;
	    String decisionName = null;
	    String storeVar = null;
	    String refVar = null;
	    String post = null;


	    while(!isEOL() && !isEOF() && ch != ']') {
		if(resBuffer.toString().equals("use")) {
		    if(storeVar != null) {
			System.out.println("Assignment and reuse are mutually exclusive.");
			System.exit(1);
		    }
		    if(reuse) {
			System.out.println("'use' is not a valid variable name.");
			System.exit(1);
		    }
		    reuse = true;

		    skipWhitespace(); //skip to the variable name
		    resBuffer = new StringBuffer();
		}
		if(ch == '=') {
		    if(reuse) {
			System.out.println("Assignment and reuse are mutually exclusive.");
		    }
		    if(storeVar != null) {
			System.out.println("Multiple assignment within a use is not valid.");
			System.exit(1);
		    }
		    storeVar = resBuffer.toString().trim();
		    if(storeVar.equals("")) {
			System.out.println("Cannot store information in an empty string");
		    }
		    skipWhitespace(); //skip to the decision name 
		    resBuffer = new StringBuffer();

		    scanChar(); //get off the '=' char
		} else { //make sure to check the character before doing anything
		    resBuffer.append(ch);
		    scanChar();
		}
	    }

	    if(isEOL()) {
		System.out.println("Unexepected EOL while parsing use");
		System.exit(1);
	    }

	    if(isEOF()) {
		System.out.println("Unexepected EOF while parsing use");
		System.exit(1);
	    }

	    if(reuse) {
		refVar = resBuffer.toString().trim();
		if(refVar.equals("")) {
		    System.out.println("Cannot retrieve information from an empty identifier");
		}
	    } else {
		decisionName = resBuffer.toString().trim();
		if(decisionName.equals("")) {
		    System.out.println("Cannot decide information from an empty identifier");
		}
	    }
	    resBuffer = new StringBuffer();

	    scanChar(); //get rid of the ending ']'

	    boolean escape = false;
	    while(escape || (!isEOL() && !isEOF() && ch != '[' && ch != '}')) {
		if(escape) {
		    if(isEOL()) { //let a / at the end of a line denote continuation
			skipWhitespace();
			escape = false;
		    }  else {
			if(isEOF()) {
			    System.out.println("Unexpected EOF while parsing use");
			    System.exit(1);
			}
			if(ch == 'n')
			    resBuffer.append('\n');
			else 
			    resBuffer.append(ch);
			scanChar();
			escape = false;
		    }
		} else if(ch == '\\') {
		    escape = true;
		    scanChar();
		} else{
		    resBuffer.append(ch);
		    scanChar();	
		}
	    }
	    post = resBuffer.toString();

	    if(isEOF()) {
		System.out.println("Unexepected EOF while parsing use");
		System.exit(1);
	    }

	    if(reuse)
		return Use.makeReferenceTo(refVar, post);
	    if(storeVar != null)
		return Use.makeSavedUse(storeVar, decisionName, post);
	    return Use.makeSingleUse(decisionName, post);
	}
    }
}
