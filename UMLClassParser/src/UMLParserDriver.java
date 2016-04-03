import java.io.File;
import java.io.IOException;
import com.github.javaparser.ParseException;

/**
 * UML Parser Driver
 * @author VaibhaviJha
 *
 */
public class UMLParserDriver {
	
	/**
	 * This is the main program that calls the parser methods
	 * @param args
	 * @throws IOException
	 * @throws ParseException
	 */
    public static void main(String args[]) throws IOException, ParseException {
        final String targetFiles = args[0];
        final String outputFile = args[1];
        GenerateGrammer grammarGenerator = new GenerateGrammer();
        DiagramGenerator diagramGenerator = new DiagramGenerator();
        File[] listofFiles = grammarGenerator.iterateFiles(targetFiles);
        System.out.println("============================Files structure=============================");
        for (File file : listofFiles) {
            System.out.println(file);
            grammarGenerator.extractElements(file);
        }
        grammarGenerator.displayGeneratedData();
        grammarGenerator.findGetSet();
        diagramGenerator.writeOutputToFile(outputFile, grammarGenerator);
        diagramGenerator.generatePNGImage(outputFile);
    }

}