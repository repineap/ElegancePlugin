import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class FileAnalyzer {

	public static final String SYSTEM_SEP = File.separator;
	private File file;
	private String filePath;
	private String name;
	private HashMap<String, Method> methodMap;
	private double loopCost, loopDepthCost, branchCost, branchDepthCost, closeRatio, overRatio;
	private HashMap<String, String> codeMap;

	public FileAnalyzer(String filePath) {
		this.file = new File(filePath);
		this.filePath = filePath;
		this.name = file.getName();
		this.methodMap = new HashMap<>();
		this.codeMap = new HashMap<>();
	}
	
	public FileAnalyzer(File file) {
		this.file = file;
		this.filePath = file.getPath();
		this.name = file.getName();
		this.methodMap = new HashMap<>();
		this.codeMap = new HashMap<>();
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

	public void generateMethodList(File fileToAnalyze) {
		CompilationUnit cu;
		try {
			cu = StaticJavaParser.parse(fileToAnalyze);
		} catch (FileNotFoundException e1) {
			cu = null;
		}
		if (cu == null) {
			return;
		}
		cu.accept(new MethodVisitor(), null);
	}

	private class MethodVisitor extends VoidVisitorAdapter<Void> {

		@Override
		public void visit(MethodDeclaration methodNode, Void arg) {
			methodMap.put(methodNode.getNameAsString(), new Method(methodNode));
			codeMap.put(methodNode.getNameAsString(), methodNode.getDeclarationAsString() +  methodNode.getBody().get().toString());
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
	
	public boolean evaluateMethods() {
		System.out.println("Analyzing file: " + this.filePath);
		HashMap<String, Double> configMap = new HashMap<>();
		final String dir = System.getProperty("user.dir");
		File file = new File(dir + SYSTEM_SEP + "configs" + SYSTEM_SEP + this.name.split("\\.")[0] + "-config.txt");
		if (!file.exists()) {
			System.out.println("No config for: " + this.name);
			return false;
		}
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
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
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
		this.generateMethodList(this.file);
		for (Method m : methodMap.values()) {
			System.out.println(codeMap.get(m.getName().split("_")[0]));
			double complexityVal = m.compareToConfig(configMap.get(m.getName().split("_")[0]), this.loopCost, this.loopDepthCost, this.branchCost, this.branchDepthCost);
			if (complexityVal <= 0 || Double.isNaN(complexityVal) || complexityVal == Double.POSITIVE_INFINITY) {
				continue;
			}
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

}
