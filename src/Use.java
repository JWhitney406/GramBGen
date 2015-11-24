public class Use {
    private String decision; //name of the decision that is used

    private String postString; //the data written after the decision
    
    private String varSource; //name of the variable that the result is already stored in
    private String varDest; //name of the variable that the result will be stored in

    public String getDecision() {
	return decision;
    }

    public String getPostString() {
	return postString;
    }

    public String getVarSource() {
	return varSource;
    }

    public String getVarDest() {
	return varDest;
    }

    public boolean isReferenceUse() {
	return varSource != null;
    }

    public boolean isSavedUse() {
	return varDest != null;
    }

    public boolean isSingleUse() {
	return !isSavedUse() && !isReferenceUse();
    }

    public static Use makeReferenceTo(String variable, String post) {
	Use res = new Use();
	res.varSource = variable;
	res.postString = post;

	return res;
    }

    public static Use makeSavedUse(String variable, String decision, String post) {
	Use res = new Use();
	res.varDest = variable;
	res.decision = decision;
	res.postString = post;

	return res;
    }

    public static Use makeSingleUse(String decision, String post) {
	Use res = new Use();
	res.decision = decision;
	res.postString = post;

	return res;
    }

    public String toString() {
	StringBuffer res = new StringBuffer();
	if(isSingleUse()) {
	    res.append(decision);
	    res.append("()");
	    res.append("+\"");
	    res.append(postString);
	    res.append("\"");
	} else if(isSavedUse()) {
	    res.append(varDest);
	    res.append(" = ");
	    res.append(decision);
	    res.append("()");
	    res.append("+\"");
	    res.append(postString);
	    res.append("\"");
	} else if(isReferenceUse()){
	    res.append("lookup(");
	    res.append(varSource);
	    res.append(")");
	    res.append("+\"");
	    res.append(postString);
	    res.append("\"");

	}
	return res.toString();
    }
}
