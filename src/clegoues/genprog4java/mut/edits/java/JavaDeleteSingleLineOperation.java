package clegoues.genprog4java.mut.edits.java;

import clegoues.genprog4java.mut.holes.java.JavaLocation;

public class JavaDeleteSingleLineOperation extends JavaDeleteOperation {

	public JavaDeleteSingleLineOperation(JavaLocation location) {
		super(location);
	}
	
	@Override
	public String toString() {
		return "StmtSingleDelete(" + this.getLocation().getId() + ")";
	}
}