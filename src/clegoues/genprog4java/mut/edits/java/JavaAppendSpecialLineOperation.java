package clegoues.genprog4java.mut.edits.java;

import clegoues.genprog4java.mut.holes.java.StatementHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.EditHole;

public class JavaAppendSpecialLineOperation extends JavaAppendOperation {

	public JavaAppendSpecialLineOperation(JavaLocation location, EditHole source) {
		super(location,source);
	}
	
	@Override
	public String toString() {
		StatementHole fixHole = (StatementHole) this.getHoleCode();
		return "StmtSpecialAppend(" + this.getLocation().getId() + "," + fixHole.getCodeBankId() + ")";
	}
}
