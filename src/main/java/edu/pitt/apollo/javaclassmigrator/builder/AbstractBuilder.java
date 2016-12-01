package edu.pitt.apollo.javaclassmigrator.builder;

import edu.pitt.apollo.javaclassmigrator.exception.BuilderException;
import edu.pitt.apollo.javaclassmigrator.util.MigrationUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by nem41 on 11/23/16.
 */
public abstract class AbstractBuilder {
    protected static final Logger logger = LoggerFactory.getLogger(SetterClassBuilder.class);
    protected static final String ABSTRACT_SETTER_CLASS_NAME = "AbstractSetter";
    protected static final String ABSTRACT_SETTER_GET_NEW_TYPE_METHOD = "getNewTypeInstance";
    private static final Set<String> abstractClasses = new HashSet<>();
    private static final Set<String> printedClassFiles = new HashSet<>();
    protected final StringBuilder stBuilder;
    protected final String outputDirectory;
    protected final String packageName;
    protected final Class newClass;
    protected final Class superClass;
    protected final String oldClassName;
    protected final Set<String> callSet;
    protected String setterName;
    protected boolean classIsAbstract = false;
    protected StringBuilder classDefinitionBuilder = new StringBuilder();

    public AbstractBuilder(Class newClass, String oldClassName, String outputDirectory,
                           String packageName, Set<String> callSet) {
        stBuilder = new StringBuilder();
        this.newClass = newClass;
        this.oldClassName = oldClassName;
        this.outputDirectory = outputDirectory;
        this.packageName = packageName;
        this.callSet = callSet;
        this.superClass = newClass.getSuperclass();
    }

    public void build() throws BuilderException {
        String classCallSetName = getClassNameForCallSet();
        setterName = getSetterName();
        if (!classSetterExists(newClass) && !callSet.contains(classCallSetName)) {
            callSet.add(classCallSetName);
            if (checkIfClassShouldBeAbstract()) {
                setClassAbstract();
            }
            buildClassDefinition();
            buildMethods();
            buildSuperClassSetter();
            completeClass();
            printSetterFile();
            printedClassFiles.add(setterName);
            callSet.remove(classCallSetName);
        }
    }

    protected abstract String getSetterName();

    protected abstract String getClassNameForCallSet();

    protected abstract void buildClassDefinition();

    protected abstract void buildMethods() throws BuilderException;

    protected abstract void completeClass();

    protected final String getStandardClassCallName(Class clazz) {
        return clazz.getCanonicalName();
    }

    protected final String getClassCallNameForFactory(Class clazz) {
        return clazz.getCanonicalName() + "Factory";
    }

    protected final boolean classSetterExists(Class subClass) {
        if (printedClassFiles.contains(subClass.getSimpleName() + "Setter")
                || printedClassFiles.contains(subClass.getSimpleName() + "SetterFactory")) {
            return true;
        }

        return false;
    }

    protected final boolean isClassIsAbstract(Class clazz) {
        return isClassIsAbstract(clazz.getSimpleName());
    }

    protected final boolean isClassIsAbstract(String classSimpleName) {
        return abstractClasses.contains(classSimpleName);
    }

    protected final void setAbstractMethodName(String methodName) {
        stBuilder.append("\tprotected abstract void ").append(methodName).append("() throws MigrationException;\n");
    }

    protected final void setClassAbstract() {
        classIsAbstract = true;
        classDefinitionBuilder = new StringBuilder();
        classDefinitionBuilder.append("public abstract class ");
        abstractClasses.add(newClass.getSimpleName());
    }

    protected static Class getListClass(Class clazz, String field) throws BuilderException {
        try {
            Field stringListField = clazz.getDeclaredField(field);
            ParameterizedType stringListType = (ParameterizedType) stringListField.getGenericType();
            Class<?> stringListClass = (Class<?>) stringListType.getActualTypeArguments()[0];
            return stringListClass;
        } catch (NoSuchFieldException ex) {
            throw new BuilderException("NoSuchFieldException accessing field " + field + " of class " + clazz.getCanonicalName());
        }
    }

    protected static void warnNoSetterMethodCanBeCreated(Class clazz, String methodName) {
        logger.warn("Cannot create setter method for " + clazz.getCanonicalName() + ", method name is :" + methodName);
    }

    protected static String getNewClassWithOldTypePackage(Class clazz, String oldClassName) {
        String packageName = oldClassName.substring(0, oldClassName.lastIndexOf("."));
        return packageName + "." + clazz.getSimpleName();
    }

    private final boolean checkIfClassShouldBeAbstract() {
        try {
            Class.forName(oldClassName);
            return false;
        } catch (ClassNotFoundException ex) {
            // old type doesn't exist, so this class must be abstract
            return true;
        }
    }

    private void buildSuperClassSetter() throws BuilderException {
        if (!superClass.getSimpleName().equals("Object")) {
            String superClassNameForCallSet = getStandardClassCallName(superClass);
            String superClassFactoryNameForCallSet = getClassCallNameForFactory(superClass);
            if (!callSet.contains(superClassNameForCallSet)
                    && !callSet.contains(superClassFactoryNameForCallSet)) {
                AbstractBuilder builder = AbstractBuilderFactory.getBuilder(superClass,
                        getNewClassWithOldTypePackage(superClass, oldClassName), outputDirectory, packageName);
                builder.build();
            }

            if (abstractClasses.contains(superClass.getSimpleName())) {
                setClassAbstract();
            }
        }
    }

    private void printSetterFile() throws BuilderException {
        MigrationUtility.writeClassToFile(stBuilder.toString(),
                outputDirectory, setterName);
    }

}
