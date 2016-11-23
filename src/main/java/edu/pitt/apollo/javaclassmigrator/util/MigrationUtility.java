package edu.pitt.apollo.javaclassmigrator.util;

import edu.pitt.apollo.javaclassmigrator.Migrator;
import org.reflections.Reflections;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * Created by nem41 on 11/23/16.
 */
public class MigrationUtility {

    public static final String ABSTRACT_SETTER_CLASS_NAME = "AbstractSetter";
    public static final String ABSTRACT_SETTER_GET_NEW_TYPE_METHOD = "getNewTypeInstance";
    private static final String ABSTRACT_SETTER_TEMPLATE = "abstract_setter_template.txt";

    public static boolean classHasSubclasses(Class clazz) {
        Reflections reflections = new Reflections(clazz.getPackage().getName());

        Set<Class> subTypes = reflections.getSubTypesOf(clazz);
        return subTypes.size() > 0;
    }

    public static void writeClassTofile(String content, String outputDirectory, String className) throws FileNotFoundException {
        File outputFile = new File(outputDirectory + File.separator + className + ".java");
        PrintStream ps = new PrintStream(outputFile);
        ps.print(content);
        ps.close();
    }

    public static void createAbstractSetterClass(String outputDirectory, String packageName) throws IOException, URISyntaxException {
        File file = new File(outputDirectory + File.separator + ABSTRACT_SETTER_CLASS_NAME + ".java");
        if (!file.exists()) {
            java.net.URL url = Migrator.class.getResource("/" + ABSTRACT_SETTER_TEMPLATE);
            java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
            String template = new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");

            String content = "package " + packageName + ";\n\n" + template;
            MigrationUtility.writeClassTofile(content, outputDirectory, ABSTRACT_SETTER_CLASS_NAME);
        }
    }
}
