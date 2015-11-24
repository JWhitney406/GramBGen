import java.io.File;
import java.io.FileNotFoundException;

import java.util.HashMap;
import java.util.Stack;

public class GrammarWriter{
    private boolean[] rawInput;
    private int pos = 0;
    public GrammarWriter(File grammarFile, boolean[] input) throws FileNotFoundException {
	Grammar gram = new Grammar(grammarFile);
	HashMap<String, String> variables = new HashMap<String, String>();

	rawInput = input;

	while(!inputUsed()) {
	    Stack decisionStack = new Stack();
	    Stack<Use> storedUseStack = new Stack<Use>(); 
	    decisionStack.push(Use.makeSingleUse("start",""));

	    while(!decisionStack.isEmpty()) {
		Object topObj = decisionStack.pop();
		//System.out.println("Pop: "+topObj);
		if(topObj instanceof Use) {
		    Use use = (Use)topObj;

		    if(storedUseStack.empty() || storedUseStack.peek() != use) {
			if(!use.isReferenceUse()) {
			    Decision top = gram.getDecision(use.getDecision());
			    if(!top.isAccumulatingDecision()) {
				Path chosenPath = top.getPaths()[choose(top.getPaths().length)];
				System.out.print(chosenPath.getSignal());
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
				int pathChoice = choose(top.getPaths().length);
				//System.out.println("Path choice: "+pathChoice);
				System.out.print(top.getPaths()[0].getSignal());
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
				}
			    }
			} else {
			    System.out.print(variables.get(use.getVarSource())+use.getPostString());
			    //also add the signal to all of the storedUses
			    for(Use u: storedUseStack) {
				String pre = variables.get(u.getVarDest());
				variables.put(u.getVarDest(), pre+variables.get(use.getVarSource())+use.getPostString());
			    } 
			}
		    } else {
			storedUseStack.pop();
			System.out.print(use.getPostString());
			//also add the signal to all of the other storedUses
			for(Use u: storedUseStack) {
			    String pre = variables.get(u.getVarDest());
			    variables.put(u.getVarDest(), pre+use.getPostString());
			}

		    }
		} else if (topObj instanceof String) {
		    System.out.print((String)topObj);
		    //also add the signal to all of the other storedUses
		    for(Use u: storedUseStack) {
			String pre = variables.get(u.getVarDest());
			variables.put(u.getVarDest(), pre+topObj);
		    }
		}
	    }
	}
    }

    public boolean inputUsed() {
	return pos >= rawInput.length;
    }
    
    public boolean getBit() {
	if(inputUsed())
	    return false;
	return rawInput[pos++];
    }

    public int choose(int max) {
	if(max == 1)
	    return 0;
	if(!getBit()) {
	    return choose(max/2);
	} else {
	    return max/2+choose(max/2+max%2);
	}
    }

    public static void main(String[] args) throws FileNotFoundException {
	if(args.length != 2) {
	    System.out.println("Usage: <grammar file> <binary input string>");
	    System.exit(1);
	}

	boolean[] input = new boolean[args[1].length()];
	for(int i = 0; i < input.length; i++) {
	    input[i] = args[1].charAt(i) != '0';
	}

	new GrammarWriter(new File(args[0]), input);
    }
}
