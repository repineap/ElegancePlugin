package main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * This does the analysis and comparison of the different files
 * @author repineap
 *
 */
public class FileAnalyzer {

	private File file;
	private String filePath;
	private String name;
	private HashMap<String, Method> methodMap;
	private double loopCost, loopDepthCost, branchCost, branchDepthCost, closeRatio, overRatio;
	private IResource resource;
	public static final double VERSION = 1.5;

	public FileAnalyzer(String filePath, IResource r) {
		this.file = new File(filePath);
		this.resource = r;
		this.filePath = filePath;
		this.name = file.getName();
		this.methodMap = new HashMap<>();
	}
	
	@Override
	public String toString() {
		String returnString = "";
		for (Method m : methodMap.values()) {
			returnString += m.getName() + ": " + m.toString() + "\n";
		}
		return returnString;
	}

	public Method getMethodFromName(String methodName) {
		return methodMap.get(methodName);
	}

	public boolean generateMethodList(File fileToAnalyze) {
		CompilationUnit cu;
		try {
			cu = StaticJavaParser.parse(fileToAnalyze);
		} catch (FileNotFoundException e1) {
			cu = null;
		} catch (Exception e) {
			cu = null;
		}
		if (cu == null) {
			return false;
		}
		cu.accept(new MethodVisitor(), null);
		return true;
	}

	private class MethodVisitor extends VoidVisitorAdapter<Void> {

		@Override
		public void visit(MethodDeclaration methodNode, Void arg) {
			methodMap.put(methodNode.getNameAsString(), new Method(methodNode));
		}
	}
	
	public String getName() {
		return this.name;
	}

	public void printTree() {
		for (Method m : methodMap.values()) {
			System.out.println(m.getName());
			m.printTree();
		}
	}
	
	private String getSrcPath(String originalPath) {
		int srcIndex = originalPath.indexOf("src");
		return originalPath.substring(0, srcIndex+3+1);
	}
	
	public boolean evaluateMethods() {
		HashMap<String, Double> configMap = new HashMap<>();
		String configPath = "";
		for (String s : filePath.split(Pattern.quote(File.separator))) {
			if (s.equals("src")) {
				break;
			}
			configPath += s + File.separator;
		}
		configPath += "configs" + File.separator + this.name.split("\\.")[0] + "-config.txt";
		File file = new File(configPath);
		if (!file.exists()) {
			return false;
		}
		try {
			BufferedReader reader = new BufferedReader(
			           new InputStreamReader(new FileInputStream(file), "UTF-8"));
			double configVersion = Double.parseDouble(reader.readLine().split(" ")[1]);
			if (configVersion < VERSION) {
				reader.close();
				return false;
			}
			String line;
            String paramLine = "";
            boolean param = true;
            while ((line = reader.readLine()) != null) {
            	if (param) {
            		paramLine = line;
            		param = !line.split(":")[0].equals("params");
            		continue;
            	}
            	String[] lineSplit = line.split(":");
                String name = lineSplit[0];
                String configScore = lineSplit[1];
                configMap.put(name, Double.parseDouble(configScore));
            }
            setParamValues(paramLine);
            reader.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return false;
		}
		if (!this.generateMethodList(this.file)) {
			return false;
		}
		FileMarker.deleteMarkers(this.resource);
		for (Method m : methodMap.values()) {
			if (!m.isImplemented()) {
				continue;
			}
			if (configMap.get(m.getName().split("_")[0]) == null) {
				continue;
			}
			double configComplexity = configMap.get(m.getName().split("_")[0]);
			double complexityVal = m.compareToConfig(configComplexity, this.loopCost, this.loopDepthCost, this.branchCost, this.branchDepthCost);
			if (complexityVal < 0 || Double.isNaN(complexityVal) || complexityVal == Double.POSITIVE_INFINITY) {
				continue;
			}
			handleComplexityVal(complexityVal, m.getDeclaration(), m.getLineNumber());
			DataSaver ds = new DataSaver(m.getName(), getSrcPath(filePath));
			ds.writeData(complexityVal + " " + overRatio + " " + m.toString());
		}
		return true;
	}
	
	private void setParamValues(String paramLine) {
		String[] paramList = paramLine.split(":")[1].split(",");
		this.loopCost = Double.parseDouble(paramList[0]);
		this.loopDepthCost = Double.parseDouble(paramList[1]);
		this.branchCost = Double.parseDouble(paramList[2]);
		this.branchDepthCost = Double.parseDouble(paramList[3]);
		this.closeRatio = Double.parseDouble(paramList[4]);
		this.overRatio = Double.parseDouble(paramList[5]);
	}

	private void handleComplexityVal(double complexityVal, String desc, int lineNumber) {
		if (complexityVal < this.closeRatio) {
			return;
		} else if (complexityVal < this.overRatio) {
			FileMarker.createMarker(this.file, this.resource, desc, lineNumber, "This method is getting complex, try to not make it more so\nScore: " + complexityVal, IMarker.SEVERITY_INFO);
		} else {
			FileMarker.createMarker(this.file, this.resource, desc, lineNumber, "This method is too complex, consider another approach\nScore: " + complexityVal, IMarker.SEVERITY_WARNING);
		}
	}

}
