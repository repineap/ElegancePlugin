import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


public class ConfigGenerator {
	
	public static final String SYSTEM_SEP = File.separator;
	public static final double VERSION = 2.0;
	public static final double LOOPCOST = 2.0;
	public static final double LOOPDEPTHCOST = 4.0;
	public static final double BRANCHCOST = 1.0;
	public static final double BRANCHDEPTHCOST = 1.5;
	public static final double CLOSEVALUE = 2.0;
	public static final double OVERVALUE = 5.0;
	private ArrayList<Method> methodList;
	
	public ConfigGenerator(File f) {
		File configFolder = new File(System.getProperty("user.dir") + SYSTEM_SEP + "configs" + SYSTEM_SEP);
		if (!configFolder.exists()) {
            if (!configFolder.mkdirs()) {
            	System.err.println("Failed to create folder");
                return;
            }
        }
		methodList = new ArrayList<>();
		String newConfigName = f.getName().split("\\.")[0] + "-config.txt";
		File configFile = new File(configFolder + SYSTEM_SEP + newConfigName);
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
			writer.println(String.format("params:%.1f,%.1f,%.1f,%.1f,%.1f,%.1f", LOOPCOST, LOOPDEPTHCOST, BRANCHCOST, BRANCHDEPTHCOST, CLOSEVALUE, OVERVALUE));
			cu.accept(new MethodVisitor(), null);
			for (Method m : methodList) {
				writer.println(String.format("%s:%.1f", m.getName(), m.calculateComplexity(LOOPCOST, LOOPDEPTHCOST, BRANCHCOST, BRANCHDEPTHCOST)));
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
}
