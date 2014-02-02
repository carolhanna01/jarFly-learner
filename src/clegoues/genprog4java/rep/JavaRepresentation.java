package clegoues.genprog4java.rep;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import clegoues.genprog4java.Fitness.FitnessValue;
import clegoues.genprog4java.Fitness.TestCase;
import clegoues.genprog4java.java.ASTUtils;
import clegoues.genprog4java.java.JavaParser;
import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.HistoryEle;
import clegoues.genprog4java.mut.JavaEditOperation;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.util.Pair;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
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


// this can handle ONE FILE right now

public class JavaRepresentation extends FaultLocRepresentation<JavaEditOperation> {

	// compile assumes that it's been written to disk.  Should it continue to assume that
	// the subdirectory has already been created?
	public static HashMap<Integer,JavaStatement> codeBank = new HashMap<Integer,JavaStatement>();
	private static HashMap<Integer,JavaStatement> base = new HashMap<Integer,JavaStatement>();
	private static CompilationUnit baseCompilationUnit = null;
	private static HashMap<Integer,ArrayList<Integer>> lineNoToAtomIDMap = new HashMap<Integer,ArrayList<Integer>>();
	private static String originalSource = "";

	private ArrayList<JavaEditOperation> genome = new ArrayList<JavaEditOperation>();

	public JavaRepresentation(ArrayList<HistoryEle> history,
			ArrayList<JavaEditOperation> genome2) {
		super(history,genome2);
	}


	public JavaRepresentation() {
		super();
	}


	private static String getOriginalSource() { return originalSource; }

	protected void instrumentForFaultLocalization(){
		// needs nothing for Java.  Don't love the "doing coverage" boolean flag 
		// thing, but it's possible I just decided it's fine.

	}

	// Java-specific coverage stuff:

	private ExecutionDataStore executionData = null;


	protected ArrayList<Integer> atomIDofSourceLine(int lineno) {
		return lineNoToAtomIDMap.get(lineno);
	}

	public TreeSet<Integer> getCoverageInfo() throws IOException
	{
		InputStream targetClass = new FileInputStream(new File(Configuration.outputDir + File.separator + "coverage"+File.separator+Configuration.packageName.replace(".","/")
				+ File.separator + Configuration.targetClassName + ".class"));

		if(executionData == null) {
			executionData = new ExecutionDataStore();
		}


		final FileInputStream in = new FileInputStream(new File("jacoco.exec"));
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
		for (final IClassCoverage cc : coverageBuilder.getClasses())
		{
			for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++)
			{
				boolean covered = false;
				switch(cc.getLine(i).getStatus()) {
				case ICounter.PARTLY_COVERED: covered = true;
				break;
				case ICounter.FULLY_COVERED: covered = true;
				break;
				default: break;
				}
				if(covered) {
					coveredLines.add(i);
				}
			}
		}
		TreeSet<Integer> atoms = new TreeSet<Integer>();
		for(int line : coveredLines) {
			ArrayList<Integer> atomIds = this.atomIDofSourceLine(line);
			if(atomIds != null && atomIds.size() >= 0) {
				atoms.addAll(atomIds); 
			}
		}

		return atoms;
	}


	public void fromSource(String fname) throws IOException
	{
		// load here, get all statements and the compilation unit saved
		// parser can visit at the same time to collect scope info
		// apparently names and types and scopes are visited here below in
		// the calls to ASTUtils

		JavaParser myParser = new JavaParser();
		JavaRepresentation.originalSource = FileUtils.readFileToString(new File(fname));
		myParser.parse(fname, Configuration.libs.split(File.pathSeparator)); 
		List<ASTNode> stmts = myParser.getStatements();
		baseCompilationUnit = myParser.getCompilationUnit();

		int stmtCounter = 0;
		for(ASTNode node : stmts)
		{
			if(JavaRepresentation.canRepair(node)) { // FIXME: I think this check was already done in the parser, but whatever
				JavaStatement s = new JavaStatement();
				s.setStmtId(stmtCounter);
				stmtCounter++;
				int lineNo = ASTUtils.getStatementLineNo(node);
				s.setLineno(lineNo);
				s.setNames(ASTUtils.getNames(node));
				s.setTypes(ASTUtils.getTypes(node));
				s.setScopes(ASTUtils.getScope(node));
				s.setASTNode(node);
				ArrayList<Integer> lineNoList = null;
				if(lineNoToAtomIDMap.containsKey(lineNo)) {
					lineNoList = lineNoToAtomIDMap.get(lineNo);
				} else {
					lineNoList = new ArrayList<Integer>();
				}
				lineNoList.add(s.getStmtId());
				lineNoToAtomIDMap.put(lineNo,  lineNoList);
				base.put(s.getStmtId(),s);
				codeBank.put(s.getStmtId(), s); 
			}
		}

	}


	public static boolean canRepair(ASTNode node) {
		return node instanceof ExpressionStatement || node instanceof AssertStatement
				|| node instanceof BreakStatement || node instanceof ContinueStatement
				|| node instanceof LabeledStatement || node instanceof ReturnStatement
				|| node instanceof ThrowStatement || node instanceof VariableDeclarationStatement
				|| node instanceof IfStatement; // FIXME: I  think we actually don't want to repair some of these things
	}


	public ArrayList<JavaEditOperation> getGenome() {
		return this.genome;
	}

	@Override
	public void loadGenomeFromString(String genome) {
		// TODO Auto-generated method stub

	}

	public void setGenome(List<JavaEditOperation> genome) {
		this.genome = (ArrayList<JavaEditOperation>) genome;
	}

	@Override
	public int genomeLength() {
		if(genome == null) { return 0 ; }
		return genome.size();
	}

	@Override
	public void serialize(String filename) {
		// TODO Auto-generated method stub

	}




	@Override
	public void outputSource(String filename) {
		// TODO Auto-generated method stub

	}


	@Override
	public int compareTo(Representation<JavaEditOperation> o) {
		// TODO Auto-generated method stub
		return 0;
	}



	@Override
	protected List<Pair<String,String>> computeSourceBuffers() {
		// FIXME: make this cache up in cacheRep
		CompilationUnit cu = baseCompilationUnit;
		Document original = new Document(JavaRepresentation.getOriginalSource()); 


		// shit OK, the problem is that the location node is from the original AST
		// and we need it in the compilation unit AST.
		// how to deal without making 1000 copies of the original AST?
		ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
		

		try
		{
			for(JavaEditOperation edit : genome) { 
				edit.edit(rewriter, cu.getAST());
			}

			TextEdit edits = null;

			edits = rewriter.rewriteAST(original, null);
			edits.apply(original);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} 
		ArrayList<Pair<String,String>> retVal = new ArrayList<Pair<String,String>>();
		retVal.add(new Pair<String,String>(Configuration.targetClassName, original.get()));
		return retVal;
	}

	@Override
	protected boolean internalTestCase(String exeName, String fileName, TestCase test) { 
		// read in the test files to get a list of test class names
		// store it in the testcase object, which will be the name
		// this is a little strange because each test class has multiple
		// test cases in it.  I think we can actually change this behavior through various
		// hacks on StackOverflow, but for the time being I just want something that works at all
		// rather than a perfect implementation.  One thing at a time, but FIXME eventually
		CommandLine command = CommandLine.parse(Configuration.javaVM);
		String filterClass = "";
		String outputDir = "";

		if(this.doingCoverage){ 
			filterClass = "clegoues.genprog4java.Fitness.CoverageFilter";
			outputDir = Configuration.outputDir + File.separator + "coverage/";
		} else {
			filterClass = "clegoues.genprog4java.Fitness.CoverageFilter"; // FIXME
			outputDir =  Configuration.outputDir + File.separator + this.getName();
		}
		String classPath = outputDir + System.getProperty("path.separator") + Configuration.libs;
		// Positive tests
		command.addArgument("-classpath");
		command.addArgument(classPath); 

		if(this.doingCoverage) {

			command.addArgument("-Xmx1024m");
			command.addArgument(
					"-javaagent:./lib/jacocoagent.jar=excludes=org.junit.*,append=false");
		} else {
			command.addArgument("-Xms128m");
			command.addArgument("-Xmx256m");
			command.addArgument("-client");
		}


		command.addArgument("clegoues.genprog4java.Fitness.UnitTestRunner");

		command.addArgument(test.toString());
		command.addArgument(filterClass);

		ExecuteWatchdog watchdog = new ExecuteWatchdog(60*6000);
		DefaultExecutor executor = new DefaultExecutor();
		String workingDirectory = System.getProperty("user.dir");
		executor.setWorkingDirectory(new File(workingDirectory));
		executor.setWatchdog(watchdog);

		ByteArrayOutputStream out = new ByteArrayOutputStream(); 

		executor.setExitValue(0);

		executor.setStreamHandler(new PumpStreamHandler(out));
		FitnessValue posFit = new FitnessValue();

		try {
			int exitValue = executor.execute(command);		
			out.flush();
			String output = out.toString();
			out.reset();

			posFit = JavaRepresentation.parseTestResults(test.toString(), output);

			this.recordFitness(test.toString(), posFit); 


		} catch (ExecuteException exception) {
			posFit.setAllPassed(false);
		} catch (Exception e)
		{
			// FIXME
			//String output = out.toString();
			//e.printStackTrace();
		}
		finally
		{
			if(out!=null)
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return posFit.isAllPassed();	
	}

	@Override
	public void delete(int location) {
		super.delete(location);
		JavaStatement locationStatement = base.get(location);
		JavaEditOperation newEdit = new JavaEditOperation(locationStatement);
		this.genome.add(newEdit);
	}

	private void editHelper(int location, int fixCode, Mutation mutType) {
		JavaStatement locationStatement = base.get(location);
		JavaStatement fixCodeStatement = codeBank.get(fixCode); // FIXME correct for Swap? Hm.
		JavaEditOperation newEdit = new JavaEditOperation(mutType,locationStatement,fixCodeStatement);
		this.genome.add(newEdit);
	}
	@Override
	public void append(int whereToAppend, int whatToAppend) {
		super.append(whereToAppend,whatToAppend);
		this.editHelper(whereToAppend,whatToAppend,Mutation.APPEND); 
	}

	@Override
	public void swap(int swap1, int swap2) {
		super.append(swap1,swap2);
		this.editHelper(swap1,swap2,Mutation.SWAP); 

	}

	@Override
	public void replace(int whatToReplace, int whatToReplaceWith) {
		super.append(whatToReplace, whatToReplaceWith);
		this.editHelper(whatToReplace,whatToReplaceWith,Mutation.REPLACE);		
	}

	public JavaRepresentation clone() throws CloneNotSupportedException {
		JavaRepresentation clone = (JavaRepresentation) super.clone();
		clone.genome = new ArrayList<JavaEditOperation>(this.genome);
		return clone;
	}

	@Override
	protected boolean internalCompile(String sourceName, String exeName) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		String program = this.computeSourceBuffers().get(0).getSecond();
		Iterable<? extends JavaFileObject> fileObjects = ASTUtils.getJavaSourceFromString(program) ; 

		LinkedList<String> options = new LinkedList<String>();

		options.add("-cp");
		options.add(Configuration.libs);

		options.add("-source");
		options.add(Configuration.sourceVersion);

		options.add("-target");
		options.add(Configuration.targetVersion);

		options.add("-d");
		String outDirName = Configuration.outputDir + File.separatorChar + exeName + File.separatorChar;
		File outDir = new File(outDirName);
		if(!outDir.exists()) 
			outDir.mkdir();
		options.add(outDirName);  
		try {
			// FIXME: can I write this in the folders to match where the class file is compiled?
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(outDirName + File.separatorChar + sourceName + Configuration.globalExtension));
			bw.write(program);
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("HERE\n");
		}


		StringWriter compilerErrorWriter = new StringWriter();

		if(!compiler.getTask(compilerErrorWriter, null, null, options, null, fileObjects).call())
		{
			compilerErrorWriter.flush();
			return false;
		}

		return true;
	}


	private static FitnessValue parseTestResults(String testClassName, String output)
	{
		String[] lines = output.split("\n");
		FitnessValue ret = new FitnessValue();
		ret.setTestClassName(testClassName);
		for(String line : lines)
		{
			try
			{
				if(line.startsWith("[SUCCESS]:"))
				{
					String[] tokens = line.split("[:\\s]+");
					ret.setAllPassed(Boolean.parseBoolean(tokens[1]));
				}
			} catch (Exception e)
			{
				ret.setAllPassed(false);
				// originally: setCompilable was false.  Necessary? FIXME
			}

			try
			{
				if(line.startsWith("[TOTAL]:"))
				{
					String[] tokens = line.split("[:\\s]+");
					ret.setNumberTests(Integer.parseInt(tokens[1]));
				}
			} catch (NumberFormatException e)
			{
				// setCompilable was false.  Why? FIXME
			}

			try
			{
				if(line.startsWith("[FAILURE]:"))
				{
					String[] tokens = line.split("[:\\s]+");
					ret.setNumTestsFailed(Integer.parseInt(tokens[1]));
				}
			} catch (NumberFormatException e)
			{
				// originally: setCompilable was false.  Why? FIXME
				// I have an inkling, having thought about it...
			}
		}

		return ret;
	}
	
	public JavaRepresentation copy() {
		JavaRepresentation copy = new JavaRepresentation(this.getHistory(), this.getGenome());

		return copy;
	}
}

