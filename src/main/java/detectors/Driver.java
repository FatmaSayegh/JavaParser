package detectors;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;


import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitor;

/**
 * 
 * @author Fatma Al Sayegh
 * @studentID 2515006A
 *
 */
public class Driver {
	private static final String FILE_PATH = "Calculator.java";

	public static void main(String args[]) {
		
		if (args.length != 1) {
			System.out.println("Invalid arguments, try");
			System.out.println("\tjava -jar coursework2.jar \"filepath\"");
			return;
		}
		
		try {
			CompilationUnit cu = JavaParser.parse(new FileInputStream(FILE_PATH));
			
			visitAndPrint("Useless Control Flows:", new UselessControlFlowDetector(), cu);
			visitAndPrint("Recursions:", new RecursionDetector(), cu);
		} catch (FileNotFoundException e) {
			System.out.println("An error occured while attempting to load the file:");
			System.out.println("\t" + e.getLocalizedMessage());
		}
	}
	
	
	/**
	 * This function simply visits and prints the breakpoints
	 * @param name The name of the action being detected
	 * @param visitor The visitor object
	 * @param cu the compilation unit
	 */
	public static void visitAndPrint(String name, VoidVisitor<List<Breakpoints>> visitor, CompilationUnit cu) {
		List<Breakpoints> collector = new ArrayList<>();
		visitor.visit(cu, collector);
		System.out.println(name);
		collector.forEach(m -> {
			System.out.println(m);
		});
	}

}
