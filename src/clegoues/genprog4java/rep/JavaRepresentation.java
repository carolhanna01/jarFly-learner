package clegoues.genprog4java.rep;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import clegoues.genprog4java.Fitness.TestCase;
import clegoues.genprog4java.java.ASTUtils;
import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.java.StatementParser;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.main.Main;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.util.GlobalUtils;
import clegoues.genprog4java.util.Pair;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;

// this can handle ONE FILE right now

public class JavaRepresentation extends FaultLocRepresentation<EditOperation> {

	// compile assumes that it's been written to disk.  Should it continue to assume that
	// the subdirectory has already been created?
	public static HashMap<Integer,ASTNode> codeBank = new HashMap<Integer,ASTNode>();
	private static ArrayList<JavaStatement> base = new ArrayList<JavaStatement>(); // FIXME: wondering if I need this
	private static CompilationUnit baseCompilationUnit = null;
	private static HashMap<Integer,ArrayList<Integer>> lineNoMap = new HashMap<Integer,ArrayList<Integer>>();


	private CommandLine testCommand = null;
	private String javaRuntime;
	private String javaVM; // FIXME
	private String libs; // FIXME
	private String filterClass = "";
	private ArrayList<EditOperation> genome = null;

	protected void instrumentForFaultLocalization(){
		String coverageOutputDir = "coverage";
		filterClass = "clegoues.genprog4java.util.CoverageFilter";

		String classPath = coverageOutputDir + File.separator + 0
				+ System.getProperty("path.separator") + libs;

		CommandLine command = CommandLine.parse(javaRuntime);
		command.addArgument("-classpath");
		command.addArgument(classPath);

		command.addArgument("-Xmx1024m");
		command.addArgument(
				"-javaagent:../lib/jacocoagent.jar=excludes=org.junit.*,append=false");

		command.addArgument("clegoues.genprog4java.Fitness.UnitTestRunner");

		//	command.addArgument(testFile);

		//	command.addArgument(filterClass);


	}

	public boolean compile(String sourceName, String exeName, String wd)
	{
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		Iterable<? extends JavaFileObject> fileObjects = null ; // FIXME: this.computeSourceBuffers();

		LinkedList<String> options = new LinkedList<String>();

		options.add("-cp");
		options.add(Configuration.libs);

		options.add("-source");
		options.add(Configuration.sourceVersion);

		options.add("-target");
		options.add(Configuration.targetVersion);

		options.add("-d");
		options.add(wd);  // e.g., tmp/10210/

		File outDir = new File(wd + File.separator);
		if(!outDir.exists())
			outDir.mkdir();		

		StringWriter compilerErrorWriter = new StringWriter();

		if(!compiler.getTask(compilerErrorWriter, null, null, options, null, fileObjects).call())
		{
			compilerErrorWriter.flush();
			System.err.println(compilerErrorWriter.getBuffer().toString());
			return false;
		}

		return true;
	}

	// Java-specific coverage stuff:

	// this all comes originally from CoverageRuntime, where I learned to use jacoco

	private static String coverageFile = "jacoco.exec";

	private IRuntime runtime;

	private ExecutionDataStore executionData;

	public TreeSet<Integer> getCoverageInfo() throws IOException
	{
		InputStream targetClass = null; // FIXME 
		if(executionData == null) {
			executionData = new ExecutionDataStore();
		}
		if(runtime == null) {
			runtime = new LoggerRuntime();
		}

		final FileInputStream in = new FileInputStream(new File(coverageFile));
		final ExecutionDataReader reader = new ExecutionDataReader(in);
		reader.setSessionInfoVisitor(new ISessionInfoVisitor() {
			public void visitSessionInfo(final SessionInfo info) {
			}
		});
		reader.setExecutionDataVisitor(new IExecutionDataVisitor() {
			public void visitClassExecution(final ExecutionData data) {
				executionData.put(data);
			}
		});

		reader.read();
		in.close();		

		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
		analyzer.analyzeClass(targetClass);

		TreeSet<Integer> coveredLines = new TreeSet<Integer>();
// OK I get this now - this indexes by line number.  Now, how are we mapping between that and ASTNodes again?
		for (final IClassCoverage cc : coverageBuilder.getClasses())
		{
			for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++)
			{
				//System.err.println("Covered? ["+i+"]: " + isCovered(cc.getLine(i).getStatus()));
				if(isCovered(cc.getLine(i).getStatus()))
				{
					coveredLines.add(i);
				}
			}
		}
// FIXME: now we need to map from covered lines back to statement IDs!

		return coveredLines;
	}
// OK, getCoverageInfo goes...one class at a time? We get classcoverage, which gives the lines in a class that are covered
	// we can map from that back to source, right?

	private boolean isCovered(final int status) {
		switch (status) {
		case ICounter.NOT_COVERED:
			return false;
		case ICounter.PARTLY_COVERED:
			return true;
		case ICounter.FULLY_COVERED:
			return true;
		}
		return false;
	}



	// FIXME: this originally had a lot of stuff about coverage in it that I don't think we need.

	public void load(String fname) throws IOException
	{
		StatementParser parser = new StatementParser();
		//  FIXME: proposed flow:
		// load here, get all statements and the compilation unit saved
		// get covered lines
		// then do a visit over the AST
		// there, do the num visitor, scopevisit, namevisit, etc
		
			parser.parse(fname, this.libs.split(File.pathSeparator)); 
			List<ASTNode> stmts = parser.getStatements();
			if(base == null) {
				base = new ArrayList<JavaStatement>();
			}
			baseCompilationUnit = parser.getCompilationUnit();
			int stmtCounter = 0;
			for(ASTNode node : stmts)
			{
				if(this.canRepair(node)) {
				JavaStatement s = new JavaStatement();
				s.setStmtId(stmtCounter);
				stmtCounter++;
				int lineNo = ASTUtils.getStatementLineNo(node);
				s.setLineno(lineNo);
				s.setNames(ASTUtils.getNames(node));
				s.setTypes(ASTUtils.getTypes(node));
				s.setScopes(ASTUtils.getScope(node));
				ASTNode copy = ASTNode.copySubtree(node.getAST(), node);
				s.setASTNode(copy);
				ArrayList<Integer> lineNoList = null;
				if(lineNoMap.containsKey(lineNo)) {
					lineNoList = lineNoMap.get(lineNo);
				} else {
					lineNoList = new ArrayList<Integer>();
				}
				lineNoList.add(s.getStmtId());
				lineNoMap.put(lineNo,  lineNoList);
				base.add(s); // FIXME: list or map? Hmm.
				codeBank.put(s.getStmtId(), s.getASTNode());
				}
		}

	}


	private static boolean canRepair(ASTNode node) {
		throw new UnsupportedOperationException();

	}

	@Override
	public ArrayList<EditOperation> getGenome() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void LoadGenomeFromString(String genome) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGenome(List<EditOperation> genome) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int genomeLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void serialize(String filename) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void debugInfo() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int maxAtom() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void computeLocalization() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fromSource(String filename) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void outputSource(String filename) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getFitness() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean fitnessIsValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reduceSearchSpace() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Representation<EditOperation> copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int num_test_evals_ignore_cache() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int compareTo(Representation<EditOperation> o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int atomIDofSourceLine(int lineno) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected List<Pair<String, String>> internalComputeSourceBuffers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Iterable<?> computeSourceBuffers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean internalTestCase(String sanityExename,
			String sanityFilename, TestCase thisTest) {
		// TODO Auto-generated method stub
		return false;
	}
}
