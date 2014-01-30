package clegoues.genprog4java.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import clegoues.genprog4java.Fitness.Fitness;
import clegoues.genprog4java.Search.Population;
import clegoues.genprog4java.Search.Search;

public class Configuration {
	public static String sourceDir = "./";
	public static String outputDir = "./";
	public static String libs;
	public static String sourceVersion = "1.5";
	public static String targetVersion = "1.5";
	public static int numPositiveTests = 5;
	public static int numNegativeTests = 1;
	public static String sanityFilename = "repair.sanity.java";
	public static String sanityExename = "repair.sanity";
	public static String globalExtension = ".java";
	public static String targetClassName = "";
	public static String searchStrategy = "ga";
	public static String javaRuntime = "";
	public static String javaVM;
	public static String posCoverageFile = "coverage.path.pos";
	public static String negCoverageFile = "coverage.path.neg";
	public static String posTestFile = "pos.test";
	public static String negTestFile = "neg.test";
	public static boolean doSanity = true;
	public static String packageName;
	
	public Configuration() {}

	public Configuration(String configFile) {
		Configuration.setProperties(configFile);
	}


	public static void setProperties(String name)
	{
		Properties prop = new Properties();
		try
		{
			prop.load(new FileReader(new File(name)));
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		if(prop.getProperty("outputDir") != null) {
			outputDir = prop.getProperty("outputDir").trim();
		}
		if(prop.getProperty("search") != null) {
			searchStrategy = prop.getProperty("search").trim();
		}
		packageName = prop.getProperty("packageName").trim();
		javaVM = prop.getProperty("javaVM").trim();
		if(prop.getProperty("sourceDir") != null) {
		sourceDir = prop.getProperty("sourceDir").trim();
		}
		javaRuntime = Runtime.getRuntime().toString(); 
		libs = prop.getProperty("libs").trim();
		
		if(prop.getProperty("sourceVersion") != null) {
		sourceVersion = prop.getProperty("sourceVersion").trim();
		}
		if(prop.getProperty("targetVersion") != null) {
		targetVersion = prop.getProperty("targetVersion").trim();
		}
		if(prop.getProperty("sanity") != null) {
			String sanity = prop.getProperty("sanity").trim();
			if(sanity == "no") {
				doSanity = false;
			}
		}
		
		if(prop.getProperty("posCoverageFile") != null)
		{
			posCoverageFile = prop.getProperty("posCoverageFile").trim();
		}
		
		if(prop.getProperty("negCoverageFile") != null)
		{
			negCoverageFile = prop.getProperty("negCoverageFile").trim();
		}
		
		
		if(prop.getProperty("positiveTests") != null)
		{
			posTestFile = prop.getProperty("positiveTests").trim();
		}
		
		if(prop.getProperty("negativeTests") != null)
		{
			negTestFile = prop.getProperty("negativeTests").trim();
		}
		
		targetClassName = prop.getProperty("targetClassName").trim();

		Search.configure(prop);
		Population.configure(prop);
		Fitness.configure(prop);

	}
}
