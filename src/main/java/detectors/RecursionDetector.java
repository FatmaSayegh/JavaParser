package detectors;

import java.util.List;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * 
 * @author Fatma Al Sayegh
 * @studentID 2515006A
 *
 */
public class RecursionDetector extends VoidVisitorAdapter<List<Breakpoints>> {

	/**
	 * This method visits the MethodCallExpr which is what calls a function<br>
	 * It then checks if the function that this MethodCallExpr is encapsulated in 
	 * has the same name. If it does, then this is either normal or polymorphic recursion:
	 * <h1>Normal Recursion:</h1>
	 * In case where the function calls it self
	 * <h1>Polymorphic Recursion:</h1>
	 * In case where the function calls another function with the same name<br>
	 * 
	 * <h1>Both cases work here as the type and arguments don't matter</h1>
	 */
	@Override
	public void visit(MethodCallExpr func, List<Breakpoints> container) {
		super.visit(func, container);
		Breakpoints bp = new Breakpoints(func);

		// if the method that this function is in DOESN't HAVE the same name, then it is not recursion
		if (!func.getNameAsString().equals(bp.getMethodName()))
			return;
		// it is recursion
		container.add(bp);

	}

}
