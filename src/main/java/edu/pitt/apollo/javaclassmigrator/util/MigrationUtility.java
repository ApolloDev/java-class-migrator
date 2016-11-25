package edu.pitt.apollo.javaclassmigrator.util;

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

    private static final String ABSTRACT_SETTER_CLASS_NAME = "AbstractSetter";
    private static final String MIGRATION_EXCEPTION_CLASS_NAME = "MigrationException";
    private static final String SETTER_INITIALIZATION_EXCEPTION_CLASS_NAME = "SetterInitializationException";
    private static final String UNSUPPORTED_TYPE_EXCEPTION_CLASS_NAME = "UnsupportedTypeException";
    private static final String ABSTRACT_SETTER_TEMPLATE = "abstract_setter_template.txt";
    private static final String MIGRATION_EXCEPTION_TEMPLATE = "migration_exception_template.txt";
    private static final String SETTER_INITIALIZATION_TEMPLATE = "setter_initialization_exception_template.txt";
    private static final String UNSUPPORTED_TYPE_EXCEPTION_TEMPLATE = "unsupported_type_exception_template.txt";

    public static boolean classHasSubclasses(Class clazz) {
        Reflections reflections = new Reflections(clazz.getPackage().getName());

        Set<Class> subTypes = reflections.getSubTypesOf(clazz);
        return subTypes.size() > 0;
    }

    public static Set<Class> getSubclassesForClass(Class clazz) {
        Reflections reflections = new Reflections(clazz.getPackage().getName());

        Set<Class> subTypes = reflections.getSubTypesOf(clazz);
        return subTypes;
    }

    private static void writeClassTofile(String content, String outputDirectory, String className) throws FileNotFoundException {
        File outputFile = new File(outputDirectory + File.separator + className + ".java");
        PrintStream ps = new PrintStream(outputFile);
        ps.print(content);
        ps.close();
    }

    public static void createInitialClassFiles(String outputDirectory, String packageName) throws IOException, URISyntaxException {
        createAbstractSetterClass(outputDirectory, packageName);
        createMigrationExceptionClass(outputDirectory, packageName);
        createSetterInitializationExceptionClass(outputDirectory, packageName);
        createUnsupportedTypeExceptionClass(outputDirectory, packageName);
    }

    private static void createAbstractSetterClass(String outputDirectory, String packageName) throws IOException, URISyntaxException {
        createJavaClassFileFromTemplate(outputDirectory, packageName, ABSTRACT_SETTER_TEMPLATE, ABSTRACT_SETTER_CLASS_NAME);
    }

    private static void createMigrationExceptionClass(String outputDirectory, String packageName) throws IOException, URISyntaxException {
        createJavaClassFileFromTemplate(outputDirectory, packageName, MIGRATION_EXCEPTION_TEMPLATE, MIGRATION_EXCEPTION_CLASS_NAME);
    }
    private static void createSetterInitializationExceptionClass(String outputDirectory, String packageName) throws IOException, URISyntaxException {
        createJavaClassFileFromTemplate(outputDirectory, packageName, SETTER_INITIALIZATION_TEMPLATE, SETTER_INITIALIZATION_EXCEPTION_CLASS_NAME);
    }

    private static void createUnsupportedTypeExceptionClass(String outputDirectory, String packageName) throws IOException, URISyntaxException {
        createJavaClassFileFromTemplate(outputDirectory, packageName, UNSUPPORTED_TYPE_EXCEPTION_TEMPLATE, UNSUPPORTED_TYPE_EXCEPTION_CLASS_NAME);
    }

    private static void createJavaClassFileFromTemplate(String outputDirectory, String packageName, String templateName, String classFileName) throws IOException, URISyntaxException {
        File file = new File(outputDirectory + File.separator + classFileName + ".java");
        if (!file.exists()) {
            String template = getTemplateContent(templateName);

            String content = "package " + packageName + ";\n\n" + template;
            MigrationUtility.writeClassTofile(content, outputDirectory, classFileName);
        }
    }

    private static String getTemplateContent(String templateName) throws IOException, URISyntaxException {
        java.net.URL url = MigrationUtility.class.getResource("/" + templateName);
        java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
        String template = new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");
        return template;

    }

}
