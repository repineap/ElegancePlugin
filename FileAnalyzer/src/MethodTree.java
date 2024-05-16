import java.util.ArrayList;

import java.util.Optional;
import java.util.Random;
import java.util.Stack;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * This class hold the representation of the methods as well as the way to construct the trees from the config files
 * @author repineap
 *
 */
public class MethodTree {
	
	private boolean implemented;

	enum type {
		root, branch, elseif, loop, methodCall
	}

	private MethodDeclaration methodNode;
	private StatementBlock rootNode;

	public MethodTree(MethodDeclaration methodNode) {
		this.methodNode = methodNode;
		this.rootNode = new StatementBlock(type.root, "root");
		ControlFlowStatementVisitor v = new ControlFlowStatementVisitor(rootNode);
		this.methodNode.accept(v, null);
		this.implemented = v.isImplemented();
	}

	public MethodTree(String encryptedString, String name) {
		buildFromString(unshiftString(encryptedString, getEncryptionKey(name)));
	}

	private void buildFromString(String treeString) {
		Stack<StatementBlock> s = new Stack<>();
		this.rootNode = new StatementBlock(type.root, "root");
		s.push(this.rootNode);
		for (int i = 2; i < treeString.length() - 1; i++) {
			StatementBlock parent = s.peek();
			if (treeString.charAt(i) == '(') {
				StatementBlock newChild = null;
				switch (treeString.charAt(i + 1)) {

				case ')':
					continue;
				case 'b':
					newChild = new StatementBlock(type.branch, "branch");
					break;
				case 'l':
					newChild = new StatementBlock(type.loop, "branch");
					break;
				case 'e':
					newChild = new StatementBlock(type.elseif, "elseif");
					break;
				}
				if (newChild != null) {
					parent.addChild(newChild);
					s.push(newChild);
				}
			} else if (treeString.charAt(i - 1) != '(' && treeString.charAt(i) == ')') {
				s.pop();
			}
		}
	}

	public int getEncryptionKey(String name) {
		int key = 0;
		for (int c : name.chars().toArray()) {
			key += c;
		}
		return key;
	}
	
	public boolean isImplemented() {
		return this.implemented;
	}

	public String unshiftString(String inputString, int key) {
		String returnString = "";
		Random r = new Random(key);
		for (int c : inputString.toCharArray()) {
			returnString += (char) (c - r.nextInt(-5, 5));
		}
		return returnString;
	}

	@Override
	public String toString() {
		return this.rootNode.toString();
	}
	
	public String toEncryptedString() {
		String returnString = "";
		String treeString = this.toString();
		int key = getEncryptionKey(methodNode.getNameAsString());
		Random r = new Random(key);
		for (int c : treeString.toCharArray()) {
			returnString += (char) (c + r.nextInt(-5, 5));
		}
		return returnString;
	}

	public double calculateComplexity(double loopCost, double loopDepthCost, double branchCost,
			double branchDepthCost) {
		return this.rootNode.calculateComplexity(loopCost, loopDepthCost, branchCost, branchDepthCost);
	}

	public void printTree() {
		this.rootNode.printTree(0);
	}

	private static class StatementBlock {

		public type type;
		public ArrayList<StatementBlock> children;
		public String name;

		public StatementBlock(type type, String name) {
			this.type = type;
			this.name = name;
			children = new ArrayList<>();
		}

		public double calculateComplexity(double loopCost, double loopDepthCost, double branchCost,
				double branchDepthCost) {
			double thisCost = 0;
			double depthMultiplier = 1;
			if (this.type == type.loop) {
				thisCost += loopCost;
				depthMultiplier = loopDepthCost;
			} else if (this.type == type.branch) {
				thisCost += branchCost;
				depthMultiplier = branchDepthCost;
			}
			for (StatementBlock child : children) {
				thisCost += depthMultiplier
						* child.calculateComplexity(loopCost, loopDepthCost, branchCost, branchDepthCost);
			}
			return thisCost;
		}

		public void addChild(StatementBlock child) {
			children.add(child);
		}

		public String toString() {
			int size = children.size();
			if (size == 0) {
				return String.format("(%s,%s)", this.type.toString().charAt(0), "()");
			}
			String[] childStrings = new String[size];
			for (int i = 0; i < size; i++) {
				childStrings[i] = children.get(i).toString();
			}
			String finalString = "";
			for (int i = 0; i < size - 1; i++) {
				finalString = finalString + String.format("%s,", childStrings[i]);
			}
			finalString = finalString + childStrings[size - 1];
			return String.format("(%s,%s)", this.type.toString().charAt(0), finalString);
		}

		public void printTree(int tabs) {
			System.out.println("\t".repeat(tabs) + String.format("%s: %s", name, type.toString()));
			for (StatementBlock child : children) {
				child.printTree(tabs + 1);
			}

		}
	}

	private static class ControlFlowStatementVisitor extends VoidVisitorAdapter<Void> {

		private StatementBlock thisBlock;
		private boolean implemented;

		public ControlFlowStatementVisitor(StatementBlock block) {
			this.thisBlock = block;
			this.implemented = true;
		}
		
		public boolean isImplemented() {
			return implemented;
		}

		@Override
		public void visit(ForStmt forNode, Void arg) {
			StatementBlock newBlock = new StatementBlock(type.loop, "for");
			thisBlock.addChild(newBlock);
			forNode.getBody().accept(new ControlFlowStatementVisitor(newBlock), null);
		}

		@Override
		public void visit(ForEachStmt eforNode, Void arg) {
			StatementBlock newBlock = new StatementBlock(type.loop, "efor");
			thisBlock.addChild(newBlock);
			eforNode.getBody().accept(new ControlFlowStatementVisitor(newBlock), null);
		}

		@Override
		public void visit(IfStmt ifNode, Void arg) {
			StatementBlock newBlock = new StatementBlock(type.branch, "if");
			thisBlock.addChild(newBlock);
			ifNode.getThenStmt().accept(new ControlFlowStatementVisitor(newBlock), null);
			Optional<Statement> elseOptional = ifNode.getElseStmt();
			if (elseOptional.isPresent()) {
				Statement elseNode = elseOptional.get();
				if (elseNode instanceof BlockStmt) {
					StatementBlock elseBlock = new StatementBlock(type.branch, "else");
					thisBlock.addChild(elseBlock);
					elseNode.accept(new ControlFlowStatementVisitor(elseBlock), null);
				} else {
					elseNode.accept(this, null);
				}

			}
		}

		@Override
		public void visit(WhileStmt whileNode, Void arg) {
			StatementBlock newBlock = new StatementBlock(type.loop, "while");
			thisBlock.addChild(newBlock);
			whileNode.getBody().accept(new ControlFlowStatementVisitor(newBlock), null);
		}

		@Override
		public void visit(ConditionalExpr ternaryExp, Void arg) {
			StatementBlock newBlock = new StatementBlock(type.branch, "ternary");
			thisBlock.addChild(newBlock);
			super.visit(ternaryExp, arg);
		}
		
		@Override
		public void visit(ThrowStmt throwExp, Void arg) {
			if (throwExp.getExpression() instanceof ObjectCreationExpr) {
	            ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr) throwExp.getExpression();

	            if ("UnsupportedOperationException".equals(objectCreationExpr.getType().getNameAsString())) {
	                this.implemented = false;
	            }
	        }
		}
	}

}
