package detectors;

import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.nodeTypes.NodeWithCondition;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * 
 * @author Fatma Al Sayegh
 * @studentID 2515006A
 *
 */
public class UselessControlFlowDetector extends VoidVisitorAdapter<List<Breakpoints>> {

	/**
	 * Visits an If statement and checks whether it's control flow is useless
	 */
	@Override
	public void visit(IfStmt stmt, List<Breakpoints> container) {
		// make sure the default visit is also called so that we wont block further
		// visits
		super.visit(stmt, container);

		Breakpoints bp = new Breakpoints(stmt);

		// if it has an empty block statement or it is if(false), then its uselss
		if (this.hasEmptyBlockStatement(stmt) || this.hasFalseCondition(stmt)) {
			container.add(bp);
			return;
		}
	}

	/**
	 * Visits a while statement and checks whether it's control flow is useless
	 */
	@Override
	public void visit(WhileStmt stmt, List<Breakpoints> container) {
		super.visit(stmt, container);

		// if it has an empty block statement, or it is while(false)
		if (this.hasEmptyBlockStatement(stmt) || this.hasFalseCondition(stmt)) {
			container.add(new Breakpoints(stmt));
			return;
		}
	}

	/**
	 * Checks if a do statement has useless flow
	 */
	@Override
	public void visit(DoStmt stmt, List<Breakpoints> container) {
		super.visit(stmt, container);
		/*
		 * if it has an empty block statement only, then its useless<br> checking if the
		 * condition is false doesn't matter because it runs at least once
		 */
		if (this.hasEmptyBlockStatement(stmt)) {
			container.add(new Breakpoints(stmt));
			return;
		}
	}

	/**
	 * Checks if a for loop has useless flow.
	 */
	@Override
	public void visit(ForStmt stmt, List<Breakpoints> container) {
		super.visit(stmt, container);
		// if it has an empty block statement
		// an ideal case would be also checking if the condition is false inside it
		if (this.hasEmptyBlockStatement(stmt)) {
			container.add(new Breakpoints(stmt));
			return;
		}
	}

	/**
	 * Checks if the switch statement's flow is useless
	 */
	@Override
	public void visit(SwitchStmt stmt, List<Breakpoints> container) {
		super.visit(stmt, container);
		/*
		 * The switch statement has SwitchEntry<br> 
		 * each SwitchEntryStmt should have a break statement, and another expression for the condition<br>
		 *  So its useless when it has no more than the break and condition statements as children
		 */
		
		// if there are any SwitchEntries which are not useless, then it is not useless as a whole
		// find all the children nodes which are switch entries that are useful, if the don't exist, then its useless
		long nonUseless = stmt.getChildNodes().stream().filter(entry -> (entry instanceof SwitchEntry) && !(isSwitchEntryUseless((SwitchEntry) entry))).count();
		// if the nonUseless entries are 0, then it is useless
		if (nonUseless == 0) {
			container.add(new Breakpoints(stmt));
		}
		
		// another way of doing that is checking if the container contains all the switch entries
	}
	
	/**
	 * Check if a switch entry in the switch statement has a useless flow
	 */
	@Override
	public void visit(SwitchEntry stmt, List<Breakpoints> container) {
		super.visit(stmt, container);

		/*
		 * if a switch entry has more children than the break and the condition statements, then
		 * it is useless
		 */
		if (isSwitchEntryUseless(stmt)) {
			container.add(new Breakpoints(stmt));
			return;
		}
	}
	/**
	 * This method determines of a switch entry is useless<br>
	 * It does so my finding all the children nodes which are not the break statement, and counting them.
	 * If its useless, then there should be 1 node for a switch entry, and 0 nodes for a default entry
	 * @param stmt
	 * @return
	 */
	private boolean isSwitchEntryUseless(SwitchEntry stmt) {
		// get the children nodes, filter them to the ones that are not break
		// if the count is 1 or 0, it is empty switch or default
		return stmt.getChildNodes().stream().filter(node -> !(node instanceof BreakStmt)).count() <= 1;
	}

	/**
	 * This method checks if a statement has a false condition<br>
	 * If the statement is non conditional, it returns false<br>
	 * 
	 * @param stmt The statement to check
	 * @returnIf it has a condition which is a boolean condition that is false
	 */
	private boolean hasFalseCondition(Statement stmt) {
		// if not node with condition, return
		if (!(stmt instanceof NodeWithCondition))
			return false;
		// case it as node with condition
		NodeWithCondition<?> nwc = (NodeWithCondition<?>) stmt;
		Expression exp = nwc.getCondition();
		// if its not a boolean literal, return false
		if (!exp.isBooleanLiteralExpr())
			return false;
		// if its not true, its false, so it returns true
		return !exp.asBooleanLiteralExpr().getValue();
	}

	/**
	 * This method check if a given statement has an empty block statement It does
	 * so by filtering the block statements to check if the have any children nodes
	 * different than comments. This determines useless control flow, how?<br>
	 * 
	 * <h1>For the if statement:</h1> An empty block statement in the if statement
	 * implies it does nothing.<br>
	 * 
	 * In case where there is an else, it is inside a block statement in the if
	 * statement, if the else is empty, then the block statement is empty <br>
	 * 
	 * With regards to else if, they are visited. <br>
	 * 
	 * <h1>For the while statement:</h1> An empty block statement implies no code
	 * 
	 * inside it, so its useless
	 */
	private boolean hasEmptyBlockStatement(Statement stmt) {
		// get all the children nodes of the statement
		for (Node n : stmt.getChildNodes()) {
			// we are interested in the code, so the block statement, so skip over anything
			// else
			if (!(n instanceof BlockStmt)) {
				continue;
			}
			// cast the block statement
			BlockStmt bs = (BlockStmt) n;
			/*
			 * if it has 0 non comment children, then it is useless
			 * 
			 * Stream over the child nodes, filter them to all the nodes that are not
			 * comments get the count of the nodes that are not comments, if its zero, then
			 * all the children nodes are either comments or non-existent
			 */
			if (bs.getChildNodes().stream().filter(node -> !(node instanceof Comment)).count() == 0) {
				return true;
			}
		}
		return false;
	}

}
