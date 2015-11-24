public class Decision {
    private String name;
    private boolean accumulate;
    private Path[] paths;

    public Decision(String name, Path[] paths, boolean accumulate) {
	this.name = name;
	this.paths = paths;
	this.accumulate = accumulate;
    }

    public boolean isAccumulatingDecision() {
	return accumulate;
    }

    public String getName() {
	return name;
    }

    public Path[] getPaths() {
	return paths;
    }

    public String toString() {
	StringBuffer resBuffer = new StringBuffer();
	resBuffer.append("Decision: ");
	resBuffer.append(name);
	resBuffer.append("\n");
	if(accumulate)
	    resBuffer.append("+");
	resBuffer.append("{");
	resBuffer.append("\n");
	for(int i = 0; i < paths.length; i++) {
	    resBuffer.append("");
	    resBuffer.append(paths[i]);
	    resBuffer.append("\n");
	}
	resBuffer.append("}");

	return resBuffer.toString();
    }
}
