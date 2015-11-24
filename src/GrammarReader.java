import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

public class GrammarReader{
    private char[] rawInput;
    private int pos = 0;

    public GrammarReader(File grammarFile, char[] input) throws FileNotFoundException {
	Grammar gram = new Grammar(grammarFile);
	HashMap<String, String> variables = new HashMap<String, String>();

	rawInput = input;

	StringBuffer output = new StringBuffer();

	while(!inputUsed()) {
	    Stack decisionStack = new Stack();
	    Stack<Use> storedUseStack = new Stack<Use>(); 
	    decisionStack.push(Use.makeSingleUse("start",""));
	    while(!decisionStack.isEmpty()) {
		Object topObj = decisionStack.pop();
		if(topObj instanceof Use) {
		    Use use = (Use)topObj;

		    if(storedUseStack.empty() || storedUseStack.peek() != use) {
			if(!use.isReferenceUse()) {
			    Decision top = gram.getDecision(use.getDecision());
			    if(!top.isAccumulatingDecision()) {
				int choice = findChoice(top);
				if(choice == -1) {
				    System.out.println("Invalid string.");
				    System.exit(1);
				}
				Path chosenPath = top.getPaths()[choice];
				output.append(bitsEncoding(choice, top.getPaths().length));

				//also add the signal to all of the storedUses
				for(Use u: storedUseStack) {
				    String pre = variables.get(u.getVarDest());
				    variables.put(u.getVarDest(), pre+chosenPath.getSignal());
				}

				if(use.isSingleUse())
				    decisionStack.push(use.getPostString());
				else if(use.isSavedUse()) {
				    variables.put(use.getVarDest(), chosenPath.getSignal());
				    storedUseStack.push(use);
				    decisionStack.push(use);
				}
				for(int i = chosenPath.getUses().length-1; i >= 0; i--) {
				    decisionStack.push(chosenPath.getUses()[i]);
				}
			    } else {
				if(use.isSavedUse()) {
				    //the signal hasn't been printed yet, so don't save it
				    variables.put(use.getVarDest(), ""); 
				    storedUseStack.push(use);
				    decisionStack.push(use);
				} else {
				    decisionStack.push(use.getPostString());
				}
				decisionStack.push(output); //save the output so we can put the result in
				                            //when we figure out what the decision was
				output = new StringBuffer();
				decisionStack.push(top);
				decisionStack.push(-1); //prepare for the accumulating
		
				/*int pathChoice = 0;
				  System.out.println("not written yet...");
				  //int pathChoice = findChoice(top);
				  //System.out.println("Path choice: "+pathChoice);
				  //System.out.print(top.getPaths()[0].getSignal());
				  //also add the signal to all of the storedUses
				  for(Use u: storedUseStack) {
				  String pre = variables.get(u.getVarDest());
				  variables.put(u.getVarDest(), pre+top.getPaths()[0].getSignal());
				  }
				
				  if(use.isSingleUse())
				  decisionStack.push(use.getPostString());
				  else if(use.isSavedUse()) {
				  variables.put(use.getVarDest(), top.getPaths()[0].getSignal());
				  storedUseStack.push(use);
				  decisionStack.push(use);
				  }
				  for(int i = pathChoice; i >= 0; i--) {
				  for(int j = top.getPaths()[i].getUses().length-1; j >= 0; j--) {
				  decisionStack.push(top.getPaths()[i].getUses()[j]);
				  }
				  if(i != 0)
				  decisionStack.push(top.getPaths()[i].getSignal());
				  }*/
			    }
			} else {
			    //System.out.println("Scanning Var ReUse:"+variables.get(use.getVarSource())+use.getPostString());
			    if(!scan(variables.get(use.getVarSource())+use.getPostString())) {
				System.out.println("Invalid string.");
				System.exit(1);			    
			    }

			    //also add the signal to all of the storedUses
			    for(Use u: storedUseStack) {
				String pre = variables.get(u.getVarDest());
				variables.put(u.getVarDest(), pre+variables.get(use.getVarSource())+use.getPostString());
			    } 
			}
		    } else {
			storedUseStack.pop();
			//System.out.println("Scanning post-string:"+use.getPostString());
			if(!scan(use.getPostString())) {
			    System.out.println("Invalid string.");
			    System.exit(1);			    
			}
			//also add the signal to all of the other storedUses
			for(Use u: storedUseStack) {
			    String pre = variables.get(u.getVarDest());
			    variables.put(u.getVarDest(), pre+use.getPostString());
			}
		    }
		} else if (topObj instanceof String) {
		    //System.out.println("Scanning post-string:"+topObj);
		    if(!scan((String)topObj)) {
			System.out.println("Invalid string.");
			System.exit(1);
		    }

		    //also add the signal to all of the other storedUses
		    for(Use u: storedUseStack) {
			String pre = variables.get(u.getVarDest());
			variables.put(u.getVarDest(), pre+topObj);
		    }
		} else if(topObj instanceof Integer) {
		    //now we're dealing with an accumulating sequence
		    int currentPath = (Integer)topObj;
		    Decision top = (Decision)decisionStack.pop();
		    //System.out.println("Scanning accumulating path "+currentPath+": "+top);
		    
		    if(currentPath+1 == top.getPaths().length ||!scan(top.getPaths()[currentPath+1].getSignal())) {
			if(currentPath == -1) {
			    System.out.println("Invalid string.");
			    System.exit(1);
			}
			StringBuffer lastRes = (StringBuffer)decisionStack.pop();
			lastRes.append(bitsEncoding(currentPath, top.getPaths().length));
			lastRes.append(output);
			output = lastRes;
		    } else {
			decisionStack.push(top);
			decisionStack.push(currentPath+1);

			//also add the signal to all of the storedUses
			for(Use u: storedUseStack) {
			    String pre = variables.get(u.getVarDest());
			    variables.put(u.getVarDest(), pre+top.getPaths()[currentPath+1].getSignal());
			}

			for(int i = top.getPaths()[currentPath+1].getUses().length-1; i >= 0; i--) {
			    decisionStack.push(top.getPaths()[currentPath+1].getUses()[i]);
			}
		    }
		}
	    }
	}
	System.out.println(output.toString());
    }

    public boolean inputUsed() {
	return pos >= rawInput.length;
    }

    //If the string exists at the current position, it will move past it
    //and return true
    //otherwise, the position will stay the same and it will return false
    public boolean scan(char[] find) {
	if(pos+find.length > rawInput.length) {//if it's too long, nope
	    return false;
	}
	
	for(int i = 0; i < find.length; i++) {
	    if(rawInput[pos+i] != find[i]) 
		return false;
	}

	pos += find.length;
	return true;
    }

    public boolean scan(String find) {
	return scan(find.toCharArray());
    }

    public int findChoice(Decision dec) {
	//System.out.println("Find choice for: "+dec);
	if(!dec.isAccumulatingDecision()) {
	    for(int i = 0; i < dec.getPaths().length; i++) {
		//System.out.println("Scanning:"+dec.getPaths()[i].getSignal());
		if(scan(dec.getPaths()[i].getSignal())) {
		    return i;
		}
	    }
	    return -1; //no match
	}
	return -1;
    }

    public static String bitsEncoding(int choice, int max) {
	if(max == 1)
	    return "";
	if(choice < max/2) {
	    return "0"+bitsEncoding(choice, max/2);
	} else {
	    return "1"+bitsEncoding(choice-max/2, max/2+max%2);
	}
    }

    /*    public int choose(int max) {
	  if(max == 1)
	  return 0;
	  if(!getBit()) {
	  return choose(max/2);
	  } else {
	  return max/2+choose(max/2+max%2);
	  }
	  }*/

    public static void main(String[] args) throws FileNotFoundException, IOException {
	if(args.length != 2) {
	    System.out.println("Usage: <grammar file> <input file>");
	    System.exit(1);
	}

	byte[] encoded = Files.readAllBytes(Paths.get(args[1]));
	String input = new String(encoded, Charset.forName("UTF-8"));

	System.out.println("Input: '"+input.toString()+"'");

	new GrammarReader(new File(args[0]), input.toString().toCharArray());
    }
}
