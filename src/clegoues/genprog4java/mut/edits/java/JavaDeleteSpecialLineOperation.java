package clegoues.genprog4java.mut.edits.java;

import clegoues.genprog4java.mut.holes.java.JavaLocation;

public class JavaDeleteSpecialLineOperation extends JavaDeleteOperation {

	public JavaDeleteSpecialLineOperation(JavaLocation location) {
		super(location);
	}
	
	@Override
	public String toString() {
		return "StmtSpecialDelete(" + this.getLocation().getId() + ")";
	}
}