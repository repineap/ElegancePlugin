package main;
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
		//Starts the process of generating the parsed tree by visiting the file
		ControlFlowStatementVisitor v = new ControlFlowStatementVisitor(rootNode);
		this.methodNode.accept(v, null);
		this.implemented = v.isImplemented();
	}

	//Legacy method from old form of complexity storage
	public MethodTree(String encryptedString, String name) {
		buildFromString(unshiftString(encryptedString, getEncryptionKey(name)));
	}

	//Legacy method for old form of complexity storage
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

	//Starts the recursive computation process from the root
	public double calculateComplexity(double loopCost, double loopDepthCost, double branchCost,
			double branchDepthCost) {
		return this.rootNode.calculateComplexity(loopCost, loopDepthCost, branchCost, branchDepthCost);
	}

	public void printTree() {
		this.rootNode.printTree(0);
	}

	/*
	 * A class used to store the structure of the file as a tree
	 * Similar to a node class in a binary tree implementation
	 */
	private static class StatementBlock {

		public type type;
		public ArrayList<StatementBlock> children;
		public String name;

		public StatementBlock(type type, String name) {
			this.type = type;
			this.name = name;
			children = new ArrayList<>();
		}

		//Computes the complexity score with the formula that is listed in the README file
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

	/*
	 * Class used to visit the java code and generate the structure starting from the root
	 * Extension of the JavaParser construct 
	 */
	private static class ControlFlowStatementVisitor extends VoidVisitorAdapter<Void> {

		//thisBlock is the parent block that all the children are added to
		private StatementBlock thisBlock;
		private boolean implemented;

		public ControlFlowStatementVisitor(StatementBlock block) {
			this.thisBlock = block;
			this.implemented = true;
		}
		
		public boolean isImplemented() {
			return implemented;
		}

		//Visits for loops, the structure of all of these is quite similar so I will only annotate this an the if node one
		@Override
		public void visit(ForStmt forNode, Void arg) {
			//Generates a new block that will represent the for loop in the body of thisBlock
			StatementBlock newBlock = new StatementBlock(type.loop, "for");
			//New block is added as a child
			thisBlock.addChild(newBlock);
			//This for loop's body is visited with it as the parent to all of the things inside of it
			forNode.getBody().accept(new ControlFlowStatementVisitor(newBlock), null);
		}

		@Override
		public void visit(ForEachStmt eforNode, Void arg) {
			StatementBlock newBlock = new StatementBlock(type.loop, "efor");
			thisBlock.addChild(newBlock);
			eforNode.getBody().accept(new ControlFlowStatementVisitor(newBlock), null);
		}

		//If statements are visited different because in the JavaParser representation else and else if statements are treated as children
		//of the if statement leading to complexity blowup
		@Override
		public void visit(IfStmt ifNode, Void arg) {
			StatementBlock newBlock = new StatementBlock(type.branch, "if");
			thisBlock.addChild(newBlock);
			//Gets the children inside of the if statment
			ifNode.getThenStmt().accept(new ControlFlowStatementVisitor(newBlock), null);
			Optional<Statement> elseOptional = ifNode.getElseStmt();
			//Checks if there is an else if or else connected to this if
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

		//Includes ternary operations as a branch statement
		@Override
		public void visit(ConditionalExpr ternaryExp, Void arg) {
			StatementBlock newBlock = new StatementBlock(type.branch, "ternary");
			thisBlock.addChild(newBlock);
			super.visit(ternaryExp, arg);
		}
		
		//Visited the UnsupportedOperationException lines to determine if the method has been implemented yet
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
