package clegoues.genprog4java.mut.edits.java;

import clegoues.genprog4java.mut.holes.java.JavaLocation;

public class JavaDeleteMultiLineOperation extends JavaDeleteOperation {

	public JavaDeleteMultiLineOperation(JavaLocation location) {
		super(location);
	}
	
	@Override
	public String toString() {
		return "StmtMultiDelete(" + this.getLocation().getId() + ")";
	}
}