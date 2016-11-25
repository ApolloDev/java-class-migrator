package edu.pitt.apollo.javaclassmigrator.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Set;

/**
 * Created by nem41 on 11/23/16.
 */
public abstract class AbstractBuilder {
    protected static final Logger logger = LoggerFactory.getLogger(SetterClassBuilder.class);
    protected static final String ABSTRACT_SETTER_CLASS_NAME = "AbstractSetter";
    protected static final String ABSTRACT_SETTER_GET_NEW_TYPE_METHOD = "getNewTypeInstance";
    protected final StringBuilder stBuilder;
    protected final String outputDirectory;
    protected final String packageName;
    protected final Class newClass;
    protected final String oldClassName;
    protected final Set<String> callSet;

    public AbstractBuilder(Class newClass, String oldClassName, String outputDirectory, String packageName, Set<String> callSet) {
        stBuilder = new StringBuilder();
        this.newClass = newClass;
        this.oldClassName = oldClassName;
        this.outputDirectory = outputDirectory;
        this.packageName = packageName;
        this.callSet = callSet;
    }

    public void build() throws FileNotFoundException {
        if (!classSetterExists(newClass) && !callSet.contains(newClass.getCanonicalName())) {
            callSet.add(newClass.getCanonicalName());
            buildClassDefinition();
            buildMethods();
            completeClass();
            printSetterFile();
            callSet.remove(newClass.getCanonicalName());
        }
    }

    protected abstract void buildClassDefinition();

    protected abstract void buildMethods() throws FileNotFoundException;

    protected abstract void completeClass();

    public abstract void printSetterFile() throws FileNotFoundException;

    protected boolean classSetterExists(Class subClass) {
        File file = new File(outputDirectory + File.separator + subClass.getSimpleName() + "Setter.java");
        if (!file.exists()) {
            File factoryFile = new File(outputDirectory + File.separator + subClass.getSimpleName() + "SetterFactory.java");
            return factoryFile.exists();
        }

        return true;
    }

    protected static Class getListClass(Class clazz, String field) throws NoSuchFieldException {
        Field stringListField = clazz.getDeclaredField(field);
        ParameterizedType stringListType = (ParameterizedType) stringListField.getGenericType();
        Class<?> stringListClass = (Class<?>) stringListType.getActualTypeArguments()[0];
        return stringListClass;
    }

    protected static void warnNoSetterClassCanBeCreated(Class clazz) {
        logger.warn("Cannot create setter class for " + clazz.getCanonicalName());
    }

    protected static void writeClassTofile(String content, String outputDirectory, String className) throws FileNotFoundException {
        File outputFile = new File(outputDirectory + File.separator + className + ".java");
        PrintStream ps = new PrintStream(outputFile);
        ps.print(content);
        ps.close();
    }

    public static String getNewClassWithOldTypePackage(Class clazz, String oldClassName) {
        String packageName = oldClassName.substring(0, oldClassName.lastIndexOf("."));
        return packageName + "." + clazz.getSimpleName();
    }

}
