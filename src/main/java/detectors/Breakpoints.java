package detectors;

import java.util.Optional;

import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
/**
 * 
 * @author Fatma Al Sayegh
 * @studentID 2515006A
 *
 */
public class Breakpoints {

	private String className;
	private String methodName;
	private int startLine;
	private int endLine;

	/**
	 * 
	 * @param className The class name of the breakpoint
	 * @param methodName The method name which the breakpoint is contained in
	 * @param startLine The start line of the breakpoint
	 * @param endLine The end line of the breakpoint
	 */
	public Breakpoints(String className, String methodName, int startLine, int endLine) {
		this.className = className;
		this.methodName = methodName;
		this.startLine = startLine;
		this.endLine = endLine;
	}

	/**
	 * Instead of a having a function that gets the class and method names and use it each time a
	 * breakpoint needs to be created, do it here<br>
	 * 
	 * The constructor in this case would extract the class and method names
	 * <br>
	 * 
	 * 
	 * @param node Node to get the information from
	 */
	public Breakpoints(Node node) {
		// Get the range for line numbers
		Optional<Range> rng = node.getRange();
		if (rng.isPresent()) {
			Range range = rng.get();
			this.startLine = range.begin.line;
			this.endLine = range.end.line;
		}
		
		// If the node has parents, and the method name or the class name are null, we have to try to load them
		while (node.getParentNode().isPresent() && (this.methodName == null || this.className == null)) {
			// get the parent node
			node = node.getParentNode().get();
			// is it a method?
			if (node instanceof MethodDeclaration) {
				// load it
				MethodDeclaration md = (MethodDeclaration) node;
				this.methodName = md.getNameAsString();
			} else if (node instanceof ClassOrInterfaceDeclaration) { // is it a class
				// load it
				ClassOrInterfaceDeclaration cid = (ClassOrInterfaceDeclaration) node;
				this.className = cid.getNameAsString();
			}
		}

		

	}

	/**
	 * This function was primarily made for RecursionDetector
	 * 
	 * @return the method name of this breakpoint
	 */
	public String getMethodName() {
		return this.methodName;
	}
	
	/**
	 * Creates a string representing all the attributes of the class
	 */
	public String toString() {
		return "ClassName=" + className + ", methodName=" + methodName + ", startline=" + startLine + ", endline="
				+ endLine;
	}
}
