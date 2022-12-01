package clegoues.genprog4java.mut.edits.java;

import clegoues.genprog4java.mut.holes.java.StatementHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.EditHole;

public class JavaAppendMultiLineOperation extends JavaAppendOperation {

	public JavaAppendMultiLineOperation(JavaLocation location, EditHole source) {
		super(location,source);
	}
	
	@Override
	public String toString() {
		StatementHole fixHole = (StatementHole) this.getHoleCode();
		return "StmtMultiAppend(" + this.getLocation().getId() + "," + fixHole.getCodeBankId() + ")";
	}
}
