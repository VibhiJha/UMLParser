import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

/**
 * Generate the Grammar
 * @author VaibhaviJha
 *
 */
public class GenerateGrammer {
    
    /* Flag for getter setter */
    boolean getFlag = false, setFlag = false;
    List < String > collectionClass = new ArrayList < > ();
    List < String > collectionInterface = new ArrayList < > ();
    List < String > dependantList;
    List < String > fieldsList;
    List < String > extendingList;
    List < String > GetSetCheck = new ArrayList < > ();
    List < String > implementingList;
    List < String > listOfMethods;
    List < String > varCollection = new ArrayList < String > ();
    Set < String > fieldsListSet = new HashSet < > ();
    Set < String > dependancySet = new HashSet < > ();  
    Map < String, List < String >> extendsMap = new HashMap < String, List < String >> ();
    Map < String, List < String >> fieldsListMap = new HashMap < String, List < String >> ();
    Map < String, List < String >> implementsMap = new HashMap < String, List < String >> ();
    Map < String, List < String >> methodsMap = new HashMap < String, List < String >> ();
    Map < String, List < String >> pdependancies = new HashMap < String, List < String >> ();

    /* get all files from the default directory*/
    //passing the command line input to fileIterator and taking only .java files
    public File[] iterateFiles(String directoryName) {
    	File dir = new File(directoryName);

    	return dir.listFiles(new FilenameFilter() { 
    	         public boolean accept(File dir, String filename)
    	              { return filename.endsWith(".java");  }
    	} );

    }

    /**
     * helper methods
     */
    /**
     * Constructor Parser
     * @param bodyDeclaration
     */
    private void constructorParser(BodyDeclaration bodyDeclaration) {
    	boolean flag = false;
        String modifier = "";
        String constructorParams = "";
        ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) bodyDeclaration;
        List < Parameter > parameters = constructorDeclaration.getParameters(); // list of all parameters
        Iterator < Parameter > iterator = parameters.iterator();
        
        switch (constructorDeclaration.getModifiers()) { // getting all the modifiers 
            case 0:
                modifier = "~";
                break;
            case 1:
                modifier = "+";
                break;
            case 2:
                modifier = "-";
                break;
            case 4:
                modifier = "#";
                break;
        }
        
        while (iterator.hasNext()) {
            StringTokenizer stringTokenizer = new StringTokenizer(iterator.next().toString());
            String tokenA = stringTokenizer.nextToken();
            String tokenB = stringTokenizer.nextToken();

            if (flag) {
                constructorParams += "," + tokenB + " : " + tokenA;
                continue;
            } else {
                constructorParams = tokenB + " : " + tokenA;
            }
            flag = true;
            dependantList.add(tokenA + " " + tokenB); //Saves parameters of other classes(basically checks dependencies)
        }
        listOfMethods.add(modifier + " " + constructorDeclaration.getName() + "(" + constructorParams + ")");

    }
    
    /**
     * Field Parser
     * @param bodyDeclaration
     * @param classInterfaceDeclaration
     */
    public void fieldParser(BodyDeclaration bodyDeclaration, ClassOrInterfaceDeclaration classInterfaceDeclaration) {
    	String modifier = "";
    	FieldDeclaration fieldDeclaration = (FieldDeclaration) bodyDeclaration;
        
        for (VariableDeclarator variableDeclarator: fieldDeclaration.getVariables()) {
            varCollection.add(classInterfaceDeclaration.getName() + " " + fieldDeclaration.getType() + " " + variableDeclarator.getId().getName());
        }

        if (ModifierSet.isPrivate(fieldDeclaration.getModifiers()) || ModifierSet.isPublic(fieldDeclaration.getModifiers())) {
            for (VariableDeclarator variableDeclaration: fieldDeclaration.getVariables()) {

                switch (fieldDeclaration.getModifiers()) {
                    case 0:
                        modifier = "~";
                        break;
                    case 1:
                        modifier = "+";
                        break;
                    case 2:
                        modifier = "-";
                        break;
                    case 4:
                        modifier = "#";
                        break;
                }


                fieldsList.add(modifier + "  " + variableDeclaration.getId().getName() + " : " + fieldDeclaration.getType());
            }
        }

    }
    
    /**
     * method parser
     * @param bodyDeclaration
     */
    private void methodParser(BodyDeclaration bodyDeclaration) {

        MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
        List < Parameter > listOfmethods = methodDeclaration.getParameters();
        for (int i = 0; i < listOfmethods.size(); i++) {
            String[] tokens = listOfmethods.get(i).toString().split(" ");
            dependantList.add(tokens[0] + " " + tokens[1]); //Saves methods also in dependentClass
        }
        //methods for public modifierset
        if ((ModifierSet.isPublic(methodDeclaration.getModifiers()))) {
        	Boolean flag = false;
        	String methodParams = "";
            List < Parameter > parameters = methodDeclaration.getParameters();
            Iterator < Parameter > iterator = parameters.iterator();
            while (iterator.hasNext()) {
                StringTokenizer stringTokenizer = new StringTokenizer(iterator.next().toString());
                String tokenA = stringTokenizer.nextToken();
                String tokenB = stringTokenizer.nextToken();
                if (flag) {
                    methodParams += "," + tokenB + " : " + tokenA;
                    continue;
                } else {
                    methodParams = tokenB + " : " + tokenA;
                }
                flag = true;
            }
            //Checks for getter and setters
            String methodName = methodDeclaration.getName().toString();
            if (methodName.startsWith("get")) {
                getFlag = true; //even if it is a private element, it goes to public classified element as getter method was found

            }
            else if (methodName.startsWith("set")) {
                setFlag = true;

            }

            listOfMethods.add("+ " + methodDeclaration.getName() + "(" + methodParams + ")" + " : " + methodDeclaration.getType());
        }
    }

    

    /**
     * Extracts elements
     * @param file
     * @throws IOException
     * @throws ParseException
     */
    public void extractElements(File file) throws IOException, ParseException {

        FileInputStream in = new FileInputStream(file);
        CompilationUnit compilationUnit; /* Compilation unit to generate the @javadoc compiltion Unit*/

        try {
            compilationUnit = JavaParser.parse( in );
            for (TypeDeclaration type: compilationUnit.getTypes()) {

                implementingList = new ArrayList < String > ();
                extendingList = new ArrayList < String > ();
                listOfMethods = new ArrayList < String > ();
                fieldsList = new ArrayList < String > ();
                dependantList = new ArrayList < String > ();


                /*Declaration of Class/Interface and extracting information from the Compilation Unit*/

                if (type instanceof ClassOrInterfaceDeclaration) { //if type is of class ClassOfInterfaceDeclaration (object or instance)
                    ClassOrInterfaceDeclaration classInterfaceDeclaration = (ClassOrInterfaceDeclaration) type;
                    if (classInterfaceDeclaration.isInterface()) {
                        collectionInterface.add(classInterfaceDeclaration.getName());
                    } else {
                        collectionClass.add(classInterfaceDeclaration.getName());
                    }

                    /* implementingList or extendingList Check*/
                    if (classInterfaceDeclaration.getExtends() != null) { // function gets all inherited classes and add saves the list in extendingList 
                        for (ClassOrInterfaceType classInterfaceType: classInterfaceDeclaration.getExtends()) {
                            extendingList.add(classInterfaceType.getName());
                        }
                    }

                    if (classInterfaceDeclaration.getImplements() != null) { // function will get all interfaces thus this line checks if there are any
                        for (ClassOrInterfaceType classInterfaceType: classInterfaceDeclaration.getImplements()) { // interfaces, add them to implementingList 
                            implementingList.add(classInterfaceType.getName());
                        }
                    }


                    /* Class Members*/

                    if (classInterfaceDeclaration.getMembers() != null) { // function gets all members(variables, functions) 
                        for (BodyDeclaration bodyDeclaration: classInterfaceDeclaration.getMembers()) {
                            if (bodyDeclaration instanceof ConstructorDeclaration) {
                                constructorParser(bodyDeclaration);
                            }

                            if (bodyDeclaration instanceof MethodDeclaration) { //if the object found is a method
                                methodParser(bodyDeclaration);
                            }

                            if (bodyDeclaration instanceof FieldDeclaration) {
                            	
                            	fieldParser(bodyDeclaration, classInterfaceDeclaration);
                            }
                        }
                    }

                    /* Adding all elements to respective collections*/
                    String name = classInterfaceDeclaration.getName();
                    
                    extendsMap.put(name, extendingList);
                    fieldsListMap.put(name, fieldsList);
                    implementsMap.put(name, implementingList);
                    methodsMap.put(name, listOfMethods);
                    pdependancies.put(name, dependantList);
                    
                    //attending getter and setter
                    if (setFlag && getFlag) {
                        GetSetCheck.add(name);
                        setFlag = false;
                        getFlag = false;
                    }
                    
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally { in .close();
        }

    }

    /**
     * get all the fields for a class
     * @param nameOfClass
     * @return String
     */
    public String getfieldsList(String nameOfClass) {
        StringBuilder outputWriter = new StringBuilder();
        for (Map.Entry < String, List < String >> entry: fieldsListMap.entrySet()) {
            if (entry.getKey().equals(nameOfClass)) {
                if (!(entry.getValue().isEmpty())) {
                    if (entry.getValue().size() > 1) {
                        for (int j = 0; j < entry.getValue().size(); j++) {
                            outputWriter.append(entry.getValue().get(j) + "\n");
                        }
                        continue;
                    }
                    outputWriter.append(entry.getValue().toString().substring(1, entry.getValue().toString().length() - 1) + "\n");
                }
            }
        }
        return outputWriter.toString();
    }

    /**
     * get all the methods
     * @param nameOfClass
     * @return String
     */
    public String getAllMethods(String nameOfClass) {
        StringBuilder outputWriter = new StringBuilder();
        for (Map.Entry < String, List < String >> entry: methodsMap.entrySet()) {
        	List <String> val = entry.getValue();
            if (entry.getKey().equals(nameOfClass)) {
                if (val.isEmpty()) {
                    continue;
                }
                if (val.size() > 1) {
                    for (int i = 0; i < val.size(); i++) {
                        outputWriter.append(val.get(i) + "\n");
                    }
                    continue;
                }

                outputWriter.append(val.toString().substring(1,val.toString().length() - 1) + "\n");
            }
        }

        return outputWriter.toString();
    }

    /**
     * Build Logic
     * @return String
     */
    public String buildLogic() {
        StringBuilder outputWriter = new StringBuilder();
        for (int i = 0; i < collectionClass.size(); i++) {
            String methods = this.getAllMethods(collectionClass.get(i));
            String fieldsList = this.getfieldsList(collectionClass.get(i));
            outputWriter.append("class " + collectionClass.get(i) + " {\n");
            outputWriter.append(fieldsList + "\n");
            outputWriter.append(methods + "\n");
            outputWriter.append("}\n");
        }

        for (int i = 0; i < collectionInterface.size(); i++) {
            String methods = this.getAllMethods(collectionInterface.get(i));
            outputWriter.append("interface " + collectionInterface.get(i) + " {\n");
            outputWriter.append(methods + "\n");
            outputWriter.append("}\n");
        }

        for (Map.Entry < String, List < String >> entry: extendsMap.entrySet()) {
            if ((entry.getValue().isEmpty())) {
                continue;
            }
            String value = entry.getValue().toString();
            outputWriter.append(value.substring(1, value.length() - 1) + "<|-- " + entry.getKey() + "\n");
        }

        for (Map.Entry < String, List < String >> entry: implementsMap.entrySet()) {
            if ((entry.getValue().isEmpty())) {
                continue;
            }
            if (entry.getValue().size() > 1) {
                for (int i = 0; i < entry.getValue().size(); i++) {
                    outputWriter.append(entry.getValue().get(i) + "<|.." + entry.getKey() + "\n");
                }
                continue;
            }
            String value = entry.getValue().toString();

            outputWriter.append(value.substring(1, value.length() - 1) + "<|.. " + entry.getKey() + "\n");
        }

        outputWriter.append(findMultiplicity());
        outputWriter.append(findDependency());
        System.out.println("Write" + " " + outputWriter);
        return outputWriter.toString();
    }
    
    
    /**
     * write depenendcy
     * @param key
     * @param string
     */
    public void printDependancy(String key, String string) {
        if (collectionClass.contains(string) || collectionInterface.contains(string)) {
            dependancySet.add(key + " " + string);
        }
    }

    
    /**
     * find multiplicity
     * @return
     */
    public String findMultiplicity() {
        StringBuilder outputWriter = new StringBuilder();
        List < String[] > multCheck = new ArrayList < String[] > ();

        for (int i = 0; i < varCollection.size(); i++) {
            String[] elements = varCollection.get(i).split(" ");
            String array[] = new String[4];


            if (collectionInterface.contains(elements[1]) || collectionClass.contains(elements[1])) {
                array[0] = elements[0];
                array[1] = elements[1];
                array[2] = "0";
                array[3] = "1";
                multCheck.add(array);
            }

            if (elements[1].startsWith("Collection")) {

                if (collectionClass.contains(elements[1].substring(elements[1].indexOf("<") + 1, elements[1].indexOf(">"))) || collectionInterface.contains(elements[1].substring(elements[1].indexOf("<") + 1, elements[1].indexOf(">")))) {
                    array[0] = elements[0];
                    array[1] = elements[1].substring(elements[1].indexOf("<") + 1, elements[1].indexOf(">"));
                    array[2] = "0";
                    array[3] = "*";
                    multCheck.add(array);
                }
            }
        }
        
        for (int m = 0; m < multCheck.size(); m++) {
            System.out.println();
            for (int n = m + 1; n < multCheck.size(); n++) {
                if (multCheck.get(m)[0].equals(multCheck.get(n)[1]) && multCheck.get(m)[1].equals(multCheck.get(n)[0])) {
                    multCheck.get(m)[2] = multCheck.get(n)[3];
                    multCheck.remove(n);
                }
            }
        }
        for (int m = 0; m < multCheck.size(); m++) {
            System.out.println();
            outputWriter.append(multCheck.get(m)[0] + " " + " \"" + multCheck.get(m)[2] + "\" - \"" + multCheck.get(m)[3] + "\" " + multCheck.get(m)[1] + "\n");
        }
        return outputWriter.toString();
    }
    
    
    
    /**
     * find the public getter and setter
     */
    public void findGetSet() {
        for (int i = 0; i < GetSetCheck.size(); i++) {
            String nameOfClass = GetSetCheck.get(i);
            for (Map.Entry < String, List < String >> entry: methodsMap.entrySet()) {
                if (entry.getKey().equals(nameOfClass)) {
                    List < String > mList = entry.getValue();
                    for (int l = 0; l < mList.size(); l++) {
                    	String s = mList.get(l).substring(5, mList.get(l).indexOf("("));
                        if (mList.get(l).contains("get") || mList.get(l).contains("set")) {
                            fieldsListSet.add(s.toLowerCase());
                        }
                    }
                    for (Map.Entry < String, List < String >> entrylist: fieldsListMap.entrySet()) {
                        if (entrylist.getKey().equals(nameOfClass)) {
                            for (int m = 0; m < entrylist.getValue().size(); m++) {
                                String s = entrylist.getValue().get(m).substring(3, entrylist.getValue().get(m).indexOf(" :")).toLowerCase();
                                if (fieldsListSet.contains(s)) {
                                	String temp =  entrylist.getValue().get(m).replace("-", "+");
                                    entrylist.getValue().remove(m);
                                    entrylist.getValue().add(temp);

                                }
                            }
                        }
                    }
                }

            }
        }
    }
    
    
    

    /**
     * find dependencies
     * @return
     */
    public String findDependency() {
        StringBuilder outputWriter = new StringBuilder();
        Boolean flag = false;
        for (Map.Entry < String, List < String >> entry: pdependancies.entrySet()) {
        	List <String> val = entry.getValue();
        	
            if (!(val.isEmpty())) {
                for (int iterator = 0; iterator < val.size(); iterator++) {
                    for (Map.Entry < String, List < String >> check: fieldsListMap.entrySet()) {
                    	List <String> checkval = check.getValue();
                        if (check.getKey().equals(entry.getKey())) {
                            if (!(checkval.isEmpty())) {
                                for (int p = 0; p < checkval.size(); p ++) {
                                    String[] compareWith = checkval.get( p ).split(" ");
                                    if (val.get(iterator).equals((compareWith)[4] + " " + compareWith[2])) {
                                        flag = true;
                                    }
                                }
                            }
                        }
                    }
                    if (!flag){
                        printDependancy(entry.getKey(), val.get(iterator).split(" ")[0]);
                    }
                }
            }
        }
        
        Iterator < String > depItr = dependancySet.iterator();
        while (depItr.hasNext()) {
        	String[] nextdep = depItr.next().toString().split(" ");
        	
            outputWriter.append(nextdep[0] + " ..> " + nextdep[1] + "\n");
        }
        return outputWriter.toString();
    }
    
    
    /**
     * Display the data
     */
    public void displayGeneratedData() {
        System.out.println("=========== Generated List===========================================");
        System.out.println("Extracted Classes : " + collectionClass);
        System.out.println("=====================================================================");
        System.out.println("Extracted Interfaces : " + collectionInterface);
        System.out.println("=====================================================================");
        System.out.println("Extracted Implements : " + implementsMap.toString());
        System.out.println("=====================================================================");
        System.out.println("Extracted Extends : " + extendsMap.toString());
        System.out.println("=====================================================================");
        System.out.println("Extracted Methods : " + methodsMap.toString());
        System.out.println("=====================================================================");
        System.out.println("Extracted fieldsList : " + fieldsListMap.toString());
        System.out.println("=====================================================================");
        System.out.println("Extracted Dependancies : " + pdependancies.toString());
    }


}