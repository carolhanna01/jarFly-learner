package clegoues.genprog4java.mut.edits.java;

import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.StatementHole;

public class JavaReplaceSpecialLineOperation extends JavaReplaceOperation {

	public JavaReplaceSpecialLineOperation(JavaLocation location, EditHole source) {
		super(location, source);
	}
	
	@Override
	public String toString() {
		StatementHole fixHole = (StatementHole) this.getHoleCode();
		return "StmtSpecialReplace(" + this.getLocation().getId() + "," + fixHole.getCodeBankId() + ")";
	}
}
