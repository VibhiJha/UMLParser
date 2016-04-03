import java.io.*;
import java.util.List;
import net.sourceforge.plantuml.GeneratedImage;
import net.sourceforge.plantuml.SourceFileReader;

/**
 * Generates the Diagram
 * @author VaibhaviJha
 *
 */
public class DiagramGenerator {
    /* Generating output file which acts as an input for PlantUML */

    public void writeOutputToFile(String outputFilePNG, GenerateGrammer grammarGenerator) throws IOException {

        File outputFile = new File(System.getProperty("user.dir"), outputFilePNG);
        String string = grammarGenerator.buildLogic();
        System.out.println("string " + " " + string);
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"))) {
            writer.write("@startuml\nskinparam classAttributeIconSize 0\n");
            writer.write(string);
            writer.write("@enduml");

        }
    }

    /* Logic for generating PNG with PlantUML and graphwiz*/

    public void generatePNGImage(String outputFile) throws IOException {
        try {
            File f1 = new File(System.getProperty("user.dir"), outputFile);
            SourceFileReader reader = new SourceFileReader(f1);
            List < GeneratedImage > list = reader.getGeneratedImages();
            File png = list.get(0).getPngFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


}