package main;
import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * This class is the bridge between FileAnalyzer and MethodTree that has the real comparison method
 * @author repineap
 *
 */
public class Method {
	private String declaration;
	private String name;
	private String content;
	private MethodTree methodTree;
	private int lineNumber;

	public Method(MethodDeclaration md) {
		this.declaration = md.getDeclarationAsString();
		this.name = md.getNameAsString();
		this.content = md.toString();
		//This is where the java files are parsed, when the MethodTree object is created
		this.methodTree = new MethodTree(md);
		this.lineNumber = md.getBegin().get().line;
	}
	
	//Used for when the solutions were stored as trees rather than as pure numbers
	public Method(String name, String encryptedString) {
		this.name = name;
		this.content = null;
		this.methodTree = new MethodTree(encryptedString, name);
	}

	public String getName() {
		return this.name;
	}
	
	public String getDeclaration() {
		return this.declaration;
	}
	
	public boolean isImplemented() {
		return this.methodTree.isImplemented();
	}

	public String getStringContent() {
		return this.content;
	}

	public MethodTree getMethodTree() {
		return this.methodTree;
	}
	
	@Override
	public String toString() {
		return this.methodTree.toString();
	}
	
	public String toEncryptedString() {
		return this.methodTree.toEncryptedString();
	}
	
	public void printTree() {
		this.methodTree.printTree();
	}
	
	public int getLineNumber() {
		return lineNumber;
	}
	
	public double calculateComplexity(double loopCost, double loopDepthCost, double branchCost, double branchDepthCost) {
		return this.methodTree.calculateComplexity(loopCost, loopDepthCost, branchCost, branchDepthCost);
	}

	
	public double compareToConfig(Double configComplexity, double loopCost, double loopDepthCost, double branchCost, double branchDepthCost) {
		//Call to the MethodTree to actually compute the value
		double thisComplexity = this.methodTree.calculateComplexity(loopCost, loopDepthCost, branchCost, branchDepthCost);
		//If there is no score for the solution, return the pure score of the user code
		if (configComplexity == 0) {
			return thisComplexity;
		}
		double complexityRatio = thisComplexity/configComplexity;
		double roundedNumber = Math.round(complexityRatio * Math.pow(10, 2)) / Math.pow(10, 2);
		return roundedNumber;
	}
}
