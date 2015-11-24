public class Path {
    private String signal;
    private Use[] uses;

    public Path(String _signal, Use[] _uses) {
	signal = _signal;
	uses = _uses;
    }

    public String getSignal() {
	return signal;
    }

    public Use[] getUses() {
	return uses;
    }

    public String toString() {
	StringBuffer res = new StringBuffer();
	res.append("Path: ");
	res.append("\"");
	res.append(signal);
	res.append("\"");
	res.append(" {");
	for(int i = 0; i < uses.length; i++) {
	    res.append("\n");
	    res.append(uses[i]);
	}
	if(uses.length != 0)
	    res.append("\n");
	res.append("}");
	return res.toString();
    }
}
