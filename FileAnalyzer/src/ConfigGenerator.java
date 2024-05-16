import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


public class ConfigGenerator {
	
	public static final double VERSION = 2.0;
//	public static final double LOOPCOST = 2.0;
//	public static final double LOOPDEPTHCOST = 4.0;
//	public static final double BRANCHCOST = 1.0;
//	public static final double BRANCHDEPTHCOST = 1.5;
//	public static final double CLOSEVALUE = 2.0;
//	public static final double OVERVALUE = 5.0;
	private double loopCost = 2.0, loopDepthCost = 4.0, branchCost = 1.0, branchDepthCost = 1.5, closeRatio = 2.0, overRatio = 5.0;
	private ArrayList<Method> methodList;
	
	public ConfigGenerator(File f, String params) {
		File configFolder = new File(System.getProperty("user.dir") + File.separator + "configs" + File.separator);
		if (!configFolder.exists()) {
            if (!configFolder.mkdirs()) {
            	System.err.println("Failed to create folder");
                return;
            }
        }
		this.setParamValues(params);
		methodList = new ArrayList<>();
		String newConfigName = f.getName().split("\\.")[0] + "-config.txt";
		File configFile = new File(configFolder + File.separator + newConfigName);
		CompilationUnit cu;
		try {
			cu = StaticJavaParser.parse(f);
		} catch (FileNotFoundException e1) {
			cu = null;
		}
		if (cu == null) {
			return;
		}
		PrintWriter writer;
		try {
			writer = new PrintWriter(configFile);
			writer.println("Version " + VERSION);
			writer.println(String.format("params:%.1f,%.1f,%.1f,%.1f,%.1f,%.1f", loopCost, loopDepthCost, branchCost, branchDepthCost, closeRatio, overRatio));
			cu.accept(new MethodVisitor(), null);
			for (Method m : methodList) {
				writer.println(String.format("%s:%.1f", m.getName(), m.calculateComplexity(loopCost, loopDepthCost, branchCost, branchDepthCost)));
			}
			writer.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("Fail");
			e.printStackTrace();
		}
		
	}
	
    private class MethodVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(MethodDeclaration methodNode, Void arg) {
            System.out.println("Method Name: " + methodNode.getName());
            Method newMethod = new Method(methodNode);
            methodList.add(newMethod);
            super.visit(methodNode, arg);
        }
    }
    
    private void setParamValues(String paramLine) {
    	if (paramLine == null) {
    		return;
    	}
		String[] paramList = paramLine.split(":")[1].split(",");
		this.loopCost = Double.parseDouble(paramList[0]);
		this.loopDepthCost = Double.parseDouble(paramList[1]);
		this.branchCost = Double.parseDouble(paramList[2]);
		this.branchDepthCost = Double.parseDouble(paramList[3]);
		this.closeRatio = Double.parseDouble(paramList[4]);
		this.overRatio = Double.parseDouble(paramList[5]);
	}
}
