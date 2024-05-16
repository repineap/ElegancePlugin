import java.io.File;

public class StudentAnalyzer {
	
	
	public StudentAnalyzer(File file) {
		FileAnalyzer analyzer = new FileAnalyzer(file);
		analyzer.evaluateMethods();
	}
}
